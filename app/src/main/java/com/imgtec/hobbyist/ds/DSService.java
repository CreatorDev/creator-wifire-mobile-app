/*
 * Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 * and/or licensors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *     conditions and the following disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written
 *     permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.imgtec.hobbyist.ds;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.imgtec.hobbyist.ds.exceptions.ConflictException;
import com.imgtec.hobbyist.ds.exceptions.DeviceServerException;
import com.imgtec.hobbyist.ds.exceptions.NetworkException;
import com.imgtec.hobbyist.ds.exceptions.NotFoundException;
import com.imgtec.hobbyist.ds.exceptions.ParseException;
import com.imgtec.hobbyist.ds.exceptions.UnauthorizedException;
import com.imgtec.hobbyist.ds.exceptions.UnknownException;
import com.imgtec.hobbyist.ds.pojo.Api;
import com.imgtec.hobbyist.ds.pojo.Bootstrap;
import com.imgtec.hobbyist.ds.pojo.Client;
import com.imgtec.hobbyist.ds.pojo.Clients;
import com.imgtec.hobbyist.ds.pojo.Configuration;
import com.imgtec.hobbyist.ds.pojo.CreatorVoid;
import com.imgtec.hobbyist.ds.pojo.EmptyResponse;
import com.imgtec.hobbyist.ds.pojo.IDPResult;
import com.imgtec.hobbyist.ds.pojo.Identities;
import com.imgtec.hobbyist.ds.pojo.Instances;
import com.imgtec.hobbyist.ds.pojo.OauthToken;
import com.imgtec.hobbyist.ds.pojo.ObjectType;
import com.imgtec.hobbyist.ds.pojo.ObjectTypes;
import com.imgtec.hobbyist.ds.pojo.PSK;
import com.imgtec.hobbyist.ds.pojo.PSKs;
import com.imgtec.hobbyist.ds.pojo.Pojo;
import com.imgtec.hobbyist.ds.pojo.UserCreatedResponse;
import com.imgtec.hobbyist.ds.pojo.UserData;
import com.imgtec.hobbyist.utils.Preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import okhttp3.Authenticator;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Encapsulates interacting with Creator Device Server. All methods doing http requests return {@link ListenableFuture}
 * that can by used by developer in either synchronous or asynchronous way.
 *
 * This class uses {@link OkHttpClient} to make http requests and {@link Gson} to serialize/deserialize payload.
 */
public class DSService {

  private OkHttpClient client;
  private Gson gson;

  private String deviceServerURL;
  private String accountsServerURL;

  private Preferences preferences;

  private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());


  public DSService(String deviceServerUrl, String accountServerUrl, Preferences preferences) {
    this.deviceServerURL = deviceServerUrl;
    this.accountsServerURL = accountServerUrl;
    this.preferences = preferences;
    client = new OkHttpClient.Builder()
        .authenticator(new Authenticator1(this, preferences))
        .addNetworkInterceptor(new OAuthInterceptor(this, preferences))
        .build();

    gson = new Gson();
  }

  /**
   * Checks whether auto login option is enabled.
   * @return true if auto login is enabled, false otherwise
   */
  public boolean isAutoLoginEnabled() {
    return preferences.getAutologin();
  }


  /**
   * Tries to login use using previously saved refresh token. Should be used when
   * {@link #isAutoLoginEnabled()} returns true.
   */
  public ListenableFuture<CreatorVoid> login() {
    final Runner<CreatorVoid> runner = new Runner<CreatorVoid>() {
      @Override
      public CreatorVoid action() {
        return loginInternal(preferences.getRefreshToken());
      }

    };
    return executor.submit(new Callable<CreatorVoid>() {
      @Override
      public CreatorVoid call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Logs user in to Device Server using token returned by browser after SSO login.
   * Once logged in, 'refresh token is saved in SharedPreferences and future attempts to login
   * should be sent to overloaded version of ths method {@link #login()}.
   * @param token returned by browser after SSO login.
   * @return {@link CreatorVoid} pojo that represents an void result.
   * @throws UnauthorizedException when password is invalid
   * @throws NetworkException in case of communication error
   */
  public ListenableFuture<CreatorVoid> login(final String token, final boolean rememberMe) {
    final Runner<CreatorVoid> runner = new Runner<CreatorVoid>() {
      @Override
      public CreatorVoid action() {
        return loginInternal(token, rememberMe);
      }

    };
    return executor.submit(new Callable<CreatorVoid>() {
      @Override
      public CreatorVoid call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Creates new user account on Creator Device Server.
   * @param username name of newly created user
   * @param password password that will be used to login
   * @param emailAddress email of newly created user
   * @return {@link UserCreatedResponse} object that contains id of newly created user.
   * @throws ConflictException if username or email address is already taken by another user.
   * @throws NetworkException in case of communication error.
   */
  public ListenableFuture<UserCreatedResponse> createAccount(final String username, final String password, final String emailAddress) {

    final Runner<UserCreatedResponse> runner = new Runner<UserCreatedResponse>() {
      @Override
      public UserCreatedResponse action() {
        return createAccountInternal(username, password, emailAddress);
      }
    };

    return executor.submit(new Callable<UserCreatedResponse>() {
      @Override
      public UserCreatedResponse call() throws Exception {
        return runner.call();
      }
    });

  }

  /**
   * Looks if client with specified is connected to Device Server and returns it.
   * @param clientName name of client to get
   * @return client object
   * @throws NotFoundException when there is no client with specified name connected to server
   * @throws NetworkException in case of communication error.
   */
  public ListenableFuture<Client> getClient(final String clientName) {
    final Runner<Client> runner = new Runner<Client>() {
      @Override
      public Client action() {
        return getClientInternal(clientName);
      }
    };

    return executor.submit(new Callable<Client>() {
      @Override
      public Client call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Gets list of client currently connected to Device Server.
   * It's developer responsibility to handle paging.
   * @param startIndex
   * @param pageSize maximum number of items that should be returned
   * @return List of connected clients or empty list if no clients are online.
   * @throws NetworkException in case of communication error
   * @return {@link Clients} object that contains list of connected clients as well as {@link com.imgtec.hobbyist.ds.pojo.PageInfo} object
   */
  public ListenableFuture<Clients> getClients(final int startIndex, final int pageSize) {

    final Runner<Clients> runner = new Runner<Clients>() {
      @Override
      public Clients action() {
        return clientsInternal(startIndex, pageSize);
      }

    };

    return executor.submit(new Callable<Clients>() {
      @Override
      public Clients call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Generates new PSK and returns it.
   * @return newly generated PSK
   * @throws NetworkException in case of communication error.
   */
  public ListenableFuture<PSK> generatePSK() {
    final Runner<PSK> runner = new Runner<PSK>() {
      @Override
      public PSK action() {
        return generatePSKInternal();
      }
    };

    return executor.submit(new Callable<PSK>() {
      @Override
      public PSK call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Returns Bootstrap Server information
   * @return {@link Bootstrap} pojo containing information about Bootstrap Server.
   */
  public ListenableFuture<Bootstrap> getBootstrap() {
    final Runner<Bootstrap> runner = new Runner<Bootstrap>() {
      @Override
      public Bootstrap action() {
        return getBootstrapInternal();
      }
    };

    return executor.submit(new Callable<Bootstrap>() {
      @Override
      public Bootstrap call() throws Exception {
        return runner.call();
      }
    });
  }


  /**
   * Returns {@link Instances} object that contains list of queried object instances.
   * In order to properly deserialize retrieved items {@link TypeToken} must be provided.
   * @param client instance of client from which objects will be queried.
   * @param objectID in IPSO standard
   * @param typeToken
   * @param <T> type of item
   * @return
   */
  public <T extends Pojo> ListenableFuture<Instances<T>> getInstances(final Client client, final int objectID, final TypeToken<Instances<T>> typeToken) {

    final Runner<Instances<T>> runner = new Runner<Instances<T>>() {
      @Override
      public Instances<T> action() {
        return getInstancesInternal(client, objectID, typeToken);
      }
    };

    return executor.submit(new Callable<Instances<T>>() {
      @Override
      public Instances<T> call() throws Exception {
        return runner.call();
      }
    });
  }

  /**
   * Updates instance of specified object on Device Server.
   * @param client instance
   * @param objectID in IPSO standard
   * @param instanceID number of object instance
   * @param data data to send
   * @param typeToken
   * @param <T>
   * @return
   */
  public <T extends Pojo> ListenableFuture<EmptyResponse> updateInstance(final Client client, final int objectID, final int instanceID, final T data, final TypeToken<T> typeToken) {
    final Runner<EmptyResponse> runner = new Runner<EmptyResponse>() {
      @Override
      public EmptyResponse action() {
        updateInstanceInternal(client, objectID, instanceID, data, typeToken);
        return new EmptyResponse();
      }
    };

    return executor.submit(new Callable<EmptyResponse>() {
      @Override
      public EmptyResponse call() throws Exception {
        return runner.call();
      }
    });
  }

  private CreatorVoid loginInternal(String token, boolean rememberMe) {
    clearAccessToken();
    Map<String, String> params = new HashMap<>();

    params.put("id_token", token);
    Request request = buildRequest("https://developer-id.flowcloud.systems", null, "POST", null, params, null, false);
    IDPResult idpResult = null;

    idpResult = execute(request, IDPResult.class);

    if (idpResult == null) {
      throw new UnauthorizedException();
    }

    request = buildRequest(deviceServerURL, null, "GET", null, null, null, false);
    Api api = execute(request, Api.class);

    params = new HashMap<>();
    params.put("username", idpResult.getKey());
    params.put("password", idpResult.getSecret());
    params.put("grant_type", "password");
    request = buildRequest(api.getLinkByRel("authenticate").getHref(), null, "POST", null, params, null, false);
    OauthToken oauthToken = execute(request, OauthToken.class);
    saveAccessToken(oauthToken);
    preferences.saveUserName(idpResult.getName());
    if (rememberMe) {
      preferences.setAutologin(true);
    }

    return new CreatorVoid();
  }

  private CreatorVoid loginInternal(String refreshToken) {
    clearAccessToken();


    Request request = buildRequest(deviceServerURL, null, "GET", null, null, null, false);
    Api api = execute(request, Api.class);

    Map<String,String> params = new HashMap<>();
    params.put("refresh_token", refreshToken);
    params.put("grant_type", "refresh_token");
    request = buildRequest(api.getLinkByRel("authenticate").getHref(), null, "POST", null, params, null, false);
    OauthToken oauthToken = execute(request, OauthToken.class);
    saveAccessToken(oauthToken);

    return new CreatorVoid();
  }

  private UserCreatedResponse createAccountInternal(String username, String password, String email) {
    Api api = requestAccountServerApi(false);
    UserData userData = new UserData();
    userData.setEmail(email);
    userData.setPassword(password);
    userData.setUsername(username);
    Request request = buildRequest(api.getLinkByRel("developers").getHref(), null, "POST", null, null, gson.toJson(userData), false);
    UserCreatedResponse response = execute(request, UserCreatedResponse.class);
    return response;
  }

  private Api requestAccountServerApi(boolean needAuthorization) {
    Request request = buildRequest(accountsServerURL, null, "GET", null, null, null, needAuthorization);
    return execute(request, Api.class);
  }

  private Api requestDeviceServerApi(boolean needAuthorization) {
    Request request = buildRequest(deviceServerURL, null, "GET", null, null, null, needAuthorization);
    return execute(request, Api.class);
  }

  private PSK generatePSKInternal() {
    Api api = requestDeviceServerApi(true);

    Request request = buildRequest(api.getLinkByRel("identities").getHref(), null, "GET", null, null, null, true);
    Identities identities = execute(request, Identities.class);

    request = buildRequest(identities.getLinkByRel("psk").getHref(), null, "GET", null, null, null, true);
    PSKs psks = execute(request, PSKs.class);

    request = buildRequest(psks.getLinkByRel("add").getHref(), null, "POST", null, null, "", true);
    PSK psk = execute(request, PSK.class);
    return psk;
  }

  private Bootstrap getBootstrapInternal() {
    Api api = requestDeviceServerApi(true);

    Request request = buildRequest(api.getLinkByRel("configuration").getHref(), null, "GET", null, null, null, true);
    Configuration configuration = execute(request, Configuration.class);

    request = buildRequest(configuration.getLinkByRel("bootstrap").getHref(), null, "GET", null, null, null, true);
    Bootstrap bootstrap = execute(request, Bootstrap.class);

    return bootstrap;
  }

  private <T extends Pojo> Instances<T> getInstancesInternal(Client client, int objectID, TypeToken<Instances<T>> typeToken) {
    Request request = buildRequest(client.getLinkByRel("objecttypes").getHref() + "?pageSize=1000", null, "GET", null, null, null, true);
    ObjectTypes objectTypes = execute(request, ObjectTypes.class);
    ObjectType objectType = null;
    for (ObjectType objectTypeTmp : objectTypes.getItems()) {
      if (objectTypeTmp.getObjectTypeID().equals(Integer.toString(objectID))) {
        objectType = objectTypeTmp;
        break;
      }
    }
    if (objectType == null) {
      throw new NotFoundException("Could not find object with id " + objectID);
    }
    request = buildRequest(objectType.getLinkByRel("instances").getHref(), null, "GET", null, null, null, true);

    Instances<T> instances = execute(request, typeToken);
    return instances;
  }

  private <T extends Pojo> void updateInstanceInternal(Client client, int objectID, int instanceID, T data, TypeToken<T> typeToken) {
    Request request = buildRequest(client.getLinkByRel("objecttypes").getHref() + "?pageSize=1000", null, "GET", null, null, null, true);
    ObjectTypes objectTypes = execute(request, ObjectTypes.class);
    ObjectType objectType = null;
    for (ObjectType objectTypeTmp : objectTypes.getItems()) {
      if (objectTypeTmp.getObjectTypeID().equals(Integer.toString(objectID))) {
        objectType = objectTypeTmp;
        break;
      }
    }
    if (objectType == null) {
      throw new NotFoundException("Could not find object with id " + objectID);
    }
    String rawData = gson.toJson(data, typeToken.getType());
    request = buildRequest(objectType.getLinkByRel("instances").getHref() + "/" + instanceID, null, "PUT", null, null, rawData, true);
    execute(request, EmptyResponse.class);
  }

  private Request buildRequest(String url, Map<String, String> queryParams, String method, Map<String, String> headers, Map<String, String> params, String rawData, boolean needAuthorization) {
    Request.Builder builder = new Request.Builder();
    if (queryParams != null) {
      url += "?";
      for (Map.Entry<String, String> entry : queryParams.entrySet()) {
        url += entry.getKey() + "=" + entry.getValue() + "&";
      }
    }
    builder.url(url);
    if (headers != null) {
      builder.headers(Headers.of(headers));
    }
    if (params != null) {
      FormBody.Builder bodyBuilder = new FormBody.Builder();
      for (Map.Entry<String, String> entry : params.entrySet()) {
        bodyBuilder.addEncoded(entry.getKey(), entry.getValue());
      }
      builder.method(method, bodyBuilder.build());
    } else if (rawData != null) {
      builder.method(method, RequestBody.create(MediaType.parse("application/json"), rawData));
    } else {
      builder.method(method, null);
    }
    builder.tag(needAuthorization);

    return builder.build();

  }

  private <T extends Pojo> T execute(Request request, TypeToken token) {
    return execute(request, null, token);
  }


  private <T extends Pojo> T execute(Request request, Class<T> returnType) {
    return execute(request, returnType, null);
  }

  private <T extends Pojo> T execute(Request request, Class<T> returnType, TypeToken token) {
    Response response = null;
    try {
      response = client.newCall(request).execute();
      if (response.code() >= 200 && response.code() < 300) {
        try {
          String responseBody = response.body().string();
          System.out.println(responseBody);
          if (token != null)
            return gson.fromJson(responseBody, token.getType());
          else
            return gson.fromJson(responseBody, returnType);
        } catch (JsonSyntaxException e) {
          throw new ParseException();
        }
      }
      switch (response.code()) {
        case 401:
          throw new UnauthorizedException();
        case 404:
          throw new NotFoundException();
        case 409:
          throw new ConflictException();
        default:
          throw new UnknownException();

      }
    } catch (IOException e) {
      throw new NetworkException();
    }
  }


  private Client getClientInternal(String clientName) {
    Clients clients = clientsInternal(0, 1000);
    for (Client client : clients.getItems()) {
      if (client.getName().equals(clientName)) {
        return client;
      }
    }
    throw new NotFoundException("Could not find client with name : [" + clientName + "]");
  }


  private Clients clientsInternal(int startIndex, int pageSize) {

    Request request = buildRequest(deviceServerURL, null, "GET", null, null, null, true);
    Api api = execute(request, Api.class);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("startIndex", Integer.toString(startIndex));
    queryParams.put("pageSize", Integer.toString(pageSize));
    request = buildRequest(api.getLinkByRel("clients").getHref(), queryParams, "GET", null, null, null, true);
    Clients clients = execute(request, Clients.class);
    return clients;
  }

  private OauthToken refreshToken() {
    Request request = buildRequest(deviceServerURL, null, "GET", null, null, null, false);
    Api api = execute(request, Api.class);

    Map<String, String> params = new HashMap<>();
    params.put("refresh_token", preferences.getRefreshToken());
    params.put("grant_type", "refresh_token");
    request = buildRequest(api.getLinkByRel("authenticate").getHref(), null, "POST", null, params, null, false);
    OauthToken oauthToken = execute(request, OauthToken.class);
    return oauthToken;
  }

  private void saveAccessToken(OauthToken oauthToken) {
    preferences.setAccessToken(oauthToken.getAccessToken());
    preferences.setAccessTokenExpiry(System.currentTimeMillis() + 1000 * oauthToken.getExpiresIn());
    preferences.setRefreshToken(oauthToken.getRefreshToken());
  }

  private void clearAccessToken() {
    preferences.setAccessToken("");
    preferences.setAccessTokenExpiry(0);
    preferences.setRefreshToken("");
  }

  static abstract class Runner<T extends Pojo> implements Callable {


    public Runner() {
    }

    public abstract T action();


    @Override
    public T call() {
      try {
        return action();
      } catch (DeviceServerException e) {
        throw e;
      } catch (Exception e) {
        throw new UnknownException(e);
      }
    }
  }

  /**
   * Reacts on 401 response from server and requests for new OAuth token from Device Server.
   */
  static class Authenticator1 implements Authenticator {

    DSService caller;
    Preferences preferences;

    Authenticator1(DSService caller, Preferences preferences) {
      this.caller = caller;
      this.preferences = preferences;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
      if (!response.request().url().toString().contains("oauth")) {
        OauthToken token = caller.refreshToken();
        preferences.setAccessToken(token.getAccessToken());
        preferences.setAccessTokenExpiry(System.currentTimeMillis() + 1000 * token.getExpiresIn());
        preferences.setRefreshToken(token.getRefreshToken());
        String newToken = token.getAccessToken();
        return response.request().newBuilder()
            .addHeader("Authorization", "Bearer " + newToken)
            .build();
      }
      return null;
    }
  }

  /**
   * An OkHttp interceptor that adds 'Authorization' header to requests.
   */
  static class OAuthInterceptor implements Interceptor {

    private Preferences preferences;
    private DSService caller;

    OAuthInterceptor(DSService caller, Preferences preferences) {
      this.preferences = preferences;
      this.caller = caller;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      long accesTokenExpiryTime = preferences.getAccessTokenExpiryTime();
      if (((Boolean) originalRequest.tag())) {
        if (accesTokenExpiryTime <= System.currentTimeMillis() - 10 * 1000) {
          OauthToken token = caller.refreshToken();
          caller.saveAccessToken(token);
        }
        Request authorisedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer " + preferences.getAccessToken())
            .build();
        return chain.proceed(authorisedRequest);
      }
      return chain.proceed(originalRequest);
    }
  }

}
