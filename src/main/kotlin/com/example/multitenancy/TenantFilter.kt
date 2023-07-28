package com.example.multitenancy

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context


@Component
class TenantFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, webFilterChain: WebFilterChain): Mono<Void> {
        return webFilterChain
            .filter(exchange)
            .contextWrite { ctx: Context ->
                ctx.put(
                    "tenantId",
                    exchange.request.queryParams["tenantId"]?.first() ?: ""
                )
            }
    }
}