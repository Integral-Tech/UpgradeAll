package net.xzos.upgradeall.core.network_api

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import net.xzos.upgradeall.core.data.json.nongson.ObjectTag
import net.xzos.upgradeall.core.data.json.nongson.ObjectTag.Companion.core
import net.xzos.upgradeall.core.route.Empty
import net.xzos.upgradeall.core.route.HttpRequestItem
import net.xzos.upgradeall.core.route.HttpResponseItem
import net.xzos.upgradeall.core.route.UpdateServerRouteGrpc


class ClientProxy {
    var id = -1

    fun newClientProxy(mChannel: ManagedChannel) {
        val blockingStub = UpdateServerRouteGrpc.newBlockingStub(mChannel)
        val asyncStub = UpdateServerRouteGrpc.newStub(mChannel)
        val httpResponse = asyncStub.newClientProxyReturn(object : StreamObserver<Empty> {
            override fun onNext(value: Empty?) {}

            override fun onError(t: Throwable?) {
                return
            }

            override fun onCompleted() {
                return
            }
        })
        val httpRequestIterator = blockingStub.newClientProxyCall(Empty.newBuilder().build())
        for (httpRequest in httpRequestIterator) {
            if (httpRequest.method == "id") {
                id = httpRequest.key.toInt()
                pushHttpResponse(httpResponse,
                        HttpResponseItem.newBuilder().setCode(0).setKey(id.toString()).build())
            } else {
                pushHttpResponse(httpResponse, getHttpResponseItem(httpRequest))
            }
        }
    }

    private fun getHttpResponseItem(request: HttpRequestItem?): HttpResponseItem {
        request ?: return HttpResponseItem.newBuilder().setCode(0).build()
        val key = request.key
        val method = request.method
        val url = request.url
        val headers = request.headersList
        val headersMap = mutableMapOf<String, String>().apply {
            for (header in headers)
                this[header.key] = header.value
        }
        val httpResponseItemBuilder = HttpResponseItem.newBuilder().setKey(key)
        val response = when (method) {
            "get" -> OkHttpApi.get(objectTag, url, headersMap)
            "post" -> {
                val body = request.body
                OkHttpApi.post(objectTag, url, headersMap, body.type, body.text)
            }
            else -> null
        } ?: return httpResponseItemBuilder.setCode(-1).build()
        return httpResponseItemBuilder.setCode(response.code).setText(response.body?.string()).build()
    }

    private fun pushHttpResponse(httpResponse: StreamObserver<HttpResponseItem>,
                                 httpResponseItem: HttpResponseItem) {
        try {
            httpResponse.onNext(httpResponseItem)
        } catch (e: RuntimeException) {
            // Cancel RPC
            httpResponse.onError(e)
        }
    }

    companion object {
        private const val TAG = "ClientProxy"
        private val objectTag = ObjectTag(core, TAG)
    }
}