from rest_framework.response import Response
from rest_framework.request import Request
from rest_framework.decorators import api_view
from django.conf import settings
import requests
import opentracing
import six
import uuid
import json

tracer = settings.OPENTRACING_TRACER

all_styles = [
    {
        "name": "Wavefront",
        "url": "WavefrontURL"
    },
    {
        "name": "BeachOps",
        "url": "BeachOpsURL"
    }]


@tracer.trace()
@api_view(http_method_names=["GET"])
def get_all_styles(request):
    return Response(all_styles, status=200)


@tracer.trace()
@api_view(http_method_names=["GET"])
def make_shirts(request, id):
    quantity = int(request.GET.get('quantity', None))
    shirts = {'shirts': ["abc", "cde"]}
    order_num = str(uuid.uuid4())
    headers = {'host': 'localhost'}
    inject_as_headers(tracer, request, headers)
    res = requests.post("http://localhost:50052/dispatch/" + order_num,
                        headers=headers, data=shirts)
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
