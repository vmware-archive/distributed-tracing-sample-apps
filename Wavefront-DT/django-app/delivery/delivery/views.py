from django.conf import settings
from django.http import HttpResponse, JsonResponse
import random
import uuid
import logging

tracer = settings.OPENTRACING_TRACER

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@tracer.trace()
def dispatch(request, order_num):
    if random.randint(1, 5) == 5:
        msg = "Random Service Unavailable!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return HttpResponse(msg, status=503)
    if random.randint(1, 10) == 10:
        order_num = None
    if not order_num:
        msg = "Invalid Order Num!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return HttpResponse(msg, status=400)
    packed_shirts = None
    if random.randint(1, 10) != 10:
        packed_shirts = dict(request.POST).get('shirts')
    if not packed_shirts:
        msg = "No shirts to deliver!"
        logging.warning(msg)
        tracer.get_span(request).set_tag("error", "true")
        return HttpResponse(msg, status=400)
    tracking_num = str(uuid.uuid4())
    logging.info("Tracking number of Order: " +
                 order_num + " is " + tracking_num)
    return JsonResponse({"orderNum": order_num,
                         "trackingNum": tracking_num,
                         "status": "shirts delivery dispatched"}, status=200)


@tracer.trace()
def retrieve(request, order_num):
    if not order_num:
        msg = "Invalid Order Num!"
        logging.warning(msg)
        return HttpResponse(msg, status=400)
    return JsonResponse({"status": "Order:" + order_num + " returned"},
                        status=200)
