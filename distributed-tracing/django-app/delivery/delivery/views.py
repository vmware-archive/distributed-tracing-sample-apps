from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.conf import settings

tracer = settings.OPENTRACING_TRACER


@tracer.trace()
@api_view(http_method_names=["POST"])
def dispatch(request, order_num):
    return Response({"status": "delivered"}, status=200)


@tracer.trace()
@api_view(http_method_names=["POST"])
def retrieve(request, order_num):
    return Response({"status": "returned"}, status=200)
