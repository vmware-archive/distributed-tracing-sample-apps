from rest_framework.response import Response
from rest_framework.request import Request
from rest_framework.decorators import api_view
from django.conf import settings
import requests
import opentracing
import six
import uuid

all_styles = [
    {
        "name": "Wavefront",
        "url": "WavefrontURL"
    },
    {
        "name": "BeachOps",
        "url": "BeachOpsURL"
    }]

tracer = settings.OPENTRACING_TRACER


@tracer.trace()
@api_view(http_method_names=["GET"])
def get_all_styles(request):
    return Response(all_styles, status=200)


@tracer.trace()
@api_view(http_method_names=["GET"])
def make_shirts(request, id):
    quantity = int(request.GET.get('quantity', None))
    shirts = []
    headers = {'host': 'localhost'}
    inject_as_headers(tracer, request, headers)
    res = requests.get("http://localhost:50052/dispatch/" + str(uuid.uuid4()),
                       headers=headers)
    return Response({"status": "completed"}, status=200)


def inject_as_headers(tracer, request, headers):
    if isinstance(request, Request):
        request = request._request
    span = tracer.get_span(request)
    text_carrier = {}
    tracer._tracer.inject(span.context, opentracing.Format.TEXT_MAP,
                          text_carrier)
    for k, v in six.iteritems(text_carrier):
        headers[k] = v
