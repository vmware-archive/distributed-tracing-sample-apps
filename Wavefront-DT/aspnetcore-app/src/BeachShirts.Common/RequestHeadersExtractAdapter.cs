using System;
using System.Collections;
using System.Collections.Generic;
using Microsoft.AspNetCore.Http;
using OpenTracing.Propagation;

namespace BeachShirts.Common
{
    public class RequestHeadersExtractAdapter : ITextMap
    {
        private readonly IHeaderDictionary headers;

        public RequestHeadersExtractAdapter(IHeaderDictionary headers)
        {
            this.headers = headers;
        }

        public IEnumerator<KeyValuePair<string, string>> GetEnumerator()
        {
            foreach (var entry in headers)
            {
                yield return new KeyValuePair<string, string>(entry.Key, entry.Value);
            }
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        public void Set(string key, string value)
        {
            throw new NotSupportedException(
                GetType().Name + " should only be used with ITracer.Extract()");
        }
    }
}
