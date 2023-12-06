# retrofit-unused-return-bug-repro
Android app reproducing an issue in minified Android apps implementing a Retrofit service (+ Kotlinx Serialization) returning an unused type.

The steps to reproduce the issue are:

1. You have an API service that looks like the next.

```kotlin
@Serializable
data class RequestBody(
    @SerialName("paramA") val paramA: String,
    @SerialName("paramB") val paramB: Int,
)

@Serializable
data class ResponseBody(
    @SerialName("status") val status: String? = null,
)

interface ExampleApiService {

    @POST("endpoint/v1")
    suspend fun postOperation(
        @Body requestBody: RequestBody,
    ): Response<ResponseBody>

    companion object {
        fun create(): ExampleApiService {
            Retrofit.Builder()
                ...
                .addConverterFactory(Json.asConverterFactory("application/json".toMediaType())) // this plays a role here too
                .build()
                .create(ExampleApiService::class.java)
        }
    }
}
```

2. You code uses the API call but ignores the response body.

```kotlin
val apiService by factory()

val response = apiService.postOperation(RequestBody("Test", 1))

if (response.isSuccessful) {
    // ignore body
}
```

3. Run this code in a minified build, and you'll get the next exception:

```
FATAL EXCEPTION: main
Process: com.vicgarci.repro, PID: 20782
java.lang.IllegalArgumentException: Unable to create converter for class java.lang.Object
  for method ExampleApiService.postOperation1
  at retrofit2.Utils.methodError(Utils.java:54)
	at retrofit2.HttpServiceMethod.createResponseConverter(HttpServiceMethod.java:126)
	at retrofit2.HttpServiceMethod.parseAnnotations(HttpServiceMethod.java:85)
	at retrofit2.ServiceMethod.parseAnnotations(ServiceMethod.java:39)
	at retrofit2.Retrofit.loadServiceMethod(Retrofit.java:202)
	at retrofit2.Retrofit$1.invoke(Retrofit.java:160)
	at java.lang.reflect.Proxy.invoke(Proxy.java:1006)
	at $Proxy2.postOperation1(Unknown Source)

...
Caused by: kotlinx.serialization.SerializationException: Serializer for class 'Any' is not found.
Please ensure that class is marked as '@Serializable' and that the serialization compiler plugin is applied.                                                                                                  
  at kotlinx.serialization.internal.PlatformKt.serializerNotRegistered(Platform.kt:31)
	at kotlinx.serialization.SerializersKt__SerializersJvmKt.serializer(SerializersJvm.kt:77)
	at kotlinx.serialization.SerializersKt.serializer(Unknown Source:1)
	at com.jakewharton.retrofit2.converter.kotlinx.serialization.Serializer.serializer(Serializer.kt:21)
	at com.jakewharton.retrofit2.converter.kotlinx.serialization.Factory.responseBodyConverter(Factory.kt:26)
	at retrofit2.Retrofit.nextResponseBodyConverter(Retrofit.java:362)
	at retrofit2.Retrofit.responseBodyConverter(Retrofit.java:345)
	at retrofit2.HttpServiceMethod.createResponseConverter(HttpServiceMethod.java:124)
	... 80 more
```

I believe the problem is caused by a combination of R8, retrofit, and kotlinx serialization:

* R8 detects the `ResponseBody` type is unused, and so it removes the class and replaces its usages with the `Object` type.
* When retrofit tries to create the responseConverter for `ExampleApiService.postOperation`, it delegates to `Json`s `ConverterFactory`, which will provide the appropriate kotlinx serialization deserializer.
* Kotlinx serialization finds that the type it needs to provide a deserializer for is `Object` (or `Any` in the Kotlin world), and fails because it does not know how to do that.
