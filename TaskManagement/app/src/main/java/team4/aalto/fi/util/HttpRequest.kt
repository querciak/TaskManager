package team4.aalto.fi.util

import okhttp3.*


class HttpRequest(var client: OkHttpClient) {

    fun GET(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    /*
    fun POST(url: String?, json: String?): String  {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.execute().use { response -> return response.body()!!.string() }
    }*/

    fun POST(url: String?, json: String?, callback: Callback): Call? {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
        //.use { response -> return response.body()!!.string() }
    }


    fun DELETE(url: String?, projectId: String, callback: Callback): Call? {
       // val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call

    }


    fun PUT(url: String?, json: String?, callback: Callback): Call? {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
        //.use { response -> return response.body()!!.string() }
    }
    companion object {
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}