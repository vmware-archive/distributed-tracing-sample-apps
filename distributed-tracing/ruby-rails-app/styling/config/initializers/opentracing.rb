require 'jaeger/client'

OpenTracing.global_tracer = Jaeger::Client.build(
    host: '127.0.0.1',
    port: 5775,
    service_name: 'styling')