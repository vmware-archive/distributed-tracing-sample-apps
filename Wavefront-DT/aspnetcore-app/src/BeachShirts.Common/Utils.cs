using System;
using System.Net.Http;
using Microsoft.AspNetCore.Mvc;

namespace BeachShirts.Common
{
    public static class Utils
    {
        public static ActionResult<T> HttpGet<T>(
            HttpClient client, string host, int port, string path)
        {
            var uri = new UriBuilder("http", host, port, path).Uri;
            var responseMessage = client.GetAsync(uri).Result;

            if (responseMessage.IsSuccessStatusCode)
            {
                return new OkObjectResult(responseMessage.Content.ReadAsAsync<T>().Result);
            }
            else
            {
                return new StatusCodeResult((int)responseMessage.StatusCode);
            }
        }

        public static ActionResult<TResult> HttpPost<TModel, TResult>(
            HttpClient client, string host, int port, string path, TModel model)
        {
            var uri = new UriBuilder("http", host, port, path).Uri;
            var responseMessage = client.PostAsJsonAsync(uri, model).Result;

            if (responseMessage.IsSuccessStatusCode)
            {
                return new OkObjectResult(responseMessage.Content.ReadAsAsync<TResult>().Result);
            }
            else
            {
                return new StatusCodeResult((int)responseMessage.StatusCode);
            }
        }
    }
}
