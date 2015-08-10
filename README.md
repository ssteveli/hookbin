# hookbin
This project inspired by [requestb.in](http://requestb.in) and [inspectb.in](http://inspectb.in) was created to
support our automation strategy at [FitPay](http://www.fit-pay.com).   RequestBin and InspectBin our awesome tools,
but we needed something just a little bit more, tailored towards QA automation.

At this point there is no UI for hookbin, it's an API driven project.  If somebody were so inspired to provide 
for a UI on top of the API... well that would be awesome.

In the spirit of an API driven strategy:

* [RAML](https://anypoint.mulesoft.com/apiplatform/repository/v2/organizations/fd8d2eae-7955-4ec9-b009-b03635fe994b/public/apis/30535/versions/32204/files/root)
* [API Portal](https://anypoint.mulesoft.com/apiplatform/fitpay/#/portals/organizations/fd8d2eae-7955-4ec9-b009-b03635fe994b/apis/30535/versions/32204/pages/45770)

## Getting Started
Here are some quick references for using the API.
### Create a webhook bucket
```
curl -X POST http://hookbin.fitpay.ninja/api/buckets
```

Sample Response
```
{
   "_links":{
      "self":{
         "href":"http://hookbin.fitpay.ninja/api/buckets/bHDywNJS8z"
      },
      "receive":{
         "href":"http://hookbin.fitpay.ninja/bHDywNJS8z"
      }
   },
   "bucketId":"bHDywNJS8z",
   "createdTsEpoch":1439212629091,
   "createdTs":"2015-08-10T13:17:09.091+0000",
   "requestCount":0,
   "ttl":359995
}
```
### Send a webhook into a bucket
```
curl -X POST -d "some sample data" http://hookbin.fitpay.ninja/bHDywNJS8z
```
### Delete a bucket
```
curl -X DELETE http://hookbin.fitpay.ninja/api/buckets/bHDywNJS8z
```
### Retrieve the last request received into a bucket
```
curl http://hookbin.fitpay.ninja/api/buckets/bHDywNJS8z/lastRequest
```
### Retrieve a list of requests received into a bucket
```
curl http://hookbin.fitpay.ninja/api/buckets/bHDywNJS8z/requests
```

