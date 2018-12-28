from rest_framework.response import Response
from rest_framework.request import Request
from rest_framework.decorators import api_view
from django.conf import settings
import random
import requests
import json
import opentracing
import logging
import six

tracer = settings.OPENTRACING_TRACER

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@tracer.trace()
@api_view(http_method_names=["GET"])
def get_shopping_menu(request):
    headers = {'host': 'localhost'}
    inject_as_headers(tracer, request, headers)
    res = requests.get("http://localhost:50051/style/",
                       headers=headers)
    if res.status_code == 200:
        return Response(res.json(), status=200)
    else:
        tracer.get_span(request).set_tag("error", "true")
        return Response(status=res.status_code)


@tracer.trace()
@api_view(http_method_names=["POST"])
def order_shirts(request):
    if random.randint(1, 5) == 5:
        msg = "Random Service Unavailable!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return Response(msg, status=503)
    data = json.loads(request.body)
    style_name = data.get("styleName", None)
    quantity = data.get("quantity", None)
    if style_name and quantity:
        res = requests.get(
            "http://localhost:50051/style/" + style_name + "/make",
            params={'quantity': quantity},
            headers=inject_as_headers(tracer, request)
        )
        if res.status_code == 200:
            return Response(res.json(), status=200)
        else:
            msg = "Failed to order shirts!"
            logging.warning(msg)
            tracer.get_span(request).set_tag("error", "true")
            return Response(msg, status=res.status_code)
    else:
        tracer.get_span(request).set_tag("error", "true")
        return Response("Missing field!", status=400)


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
