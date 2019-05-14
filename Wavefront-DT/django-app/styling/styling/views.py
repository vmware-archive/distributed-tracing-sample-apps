from rest_framework.response import Response
from rest_framework.request import Request
from rest_framework.decorators import api_view
from django.conf import settings
import requests
import opentracing
import six
import uuid
import random
import logging

tracer = settings.OPENTRACING_TRACER

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

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
    if random.randint(1, 5) == 5:
        msg = "Random Service Unavailable!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return Response(msg, status=503)
    quantity = int(request.GET.get('quantity', None))
    shirts = []
    for i in range(0, quantity):
        shirts.append({"name": id, "imageUrl": id + "-image"})
    order_num = str(uuid.uuid4())
    res = requests.post("http://localhost:50052/dispatch/" + order_num,
                        headers=inject_as_headers(tracer, request),
                        data={'shirts': shirts})
    if res.status_code < 400:
        return Response(res.json(), status=200)
    else:
        msg = "Failed to make shirts!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return Response(msg, status=res.status_code)


def inject_as_headers(tracer, request):
    headers = {}
    if isinstance(request, Request):
        request = request._request
    span = tracer.get_span(request)
    text_carrier = {}
    tracer._tracer.inject(span.context, opentracing.Format.TEXT_MAP,
                          text_carrier)
    for k, v in six.iteritems(text_carrier):
        headers[k] = v
