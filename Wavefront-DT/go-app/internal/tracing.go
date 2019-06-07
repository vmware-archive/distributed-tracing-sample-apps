// 2019. Mahesh Voleti (mvoleti@vmware.com)

package internal

import (
	"io"
	"io/ioutil"
	"log"
	"net/http"

	opentracing "github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"
)

func NewGlobalTracer(serviceName string) io.Closer {
	//TODO: replace with WavefrontTracer
	tracer := opentracing.NoopTracer{}
	opentracing.SetGlobalTracer(tracer)
	return ioutil.NopCloser(nil)
}

func NewServerSpan(req *http.Request, spanName string) opentracing.Span {
	tracer := opentracing.GlobalTracer()
	parentCtx, err := tracer.Extract(opentracing.HTTPHeaders, opentracing.HTTPHeadersCarrier(req.Header))
	var span opentracing.Span
	if err == nil { // has parent context
		span = tracer.StartSpan(spanName, opentracing.ChildOf(parentCtx))
	} else if err == opentracing.ErrSpanContextNotFound { // no parent
		span = tracer.StartSpan(spanName)
	} else {
		log.Printf("Error in extracting tracer context: %s", err.Error())
	}

	otrext.SpanKindRPCServer.Set(span)

	return span
}
