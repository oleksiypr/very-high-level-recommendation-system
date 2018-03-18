## Very High Level Recommendation System

Implement RESTful service with Akka, Akka HTTP, Cats which serves as a very high level recommendation system.
There should be two requests with corresponding responses.

1. ###Register user

    request should be of the following format:
     
    `POST`
    ```json
    {
       "userName": "David", 
       "email": "david@gmail.com", 
       "age": 28, 
       "gender": 1
    }
    ```
    
    request sould be validated:
    - email
    - age range 5-120
    - gender in [1, 2], where 1 for male, 2 for female
    
    Response should be of format:
    
    `200 OK` 
    ```json
    { 
       "userId": 9797345, 
       "videoId":4324556 
    }
    ```

2. ###Action
    *Action* is a response to *Register*, reaction on sent video.
    
    `POST`
    ```json
    {
       "userId": 9797345, 
       "videoId":4324556, 
       "actionId": 3
    }
    ```
 
    where `userId` and `videoId` correspond to *Register* response.
    Request should be validated for existing `userId`, `videoId` and 
    `actionId`.
    
    Response should of format: 
    
    `200 OK`
    ```json
    {
       "userId": 9797345, 
       "videoId":6454556
    }
    ```

For both requests there should be following error response format: 

`400 Bad Request` 
```json
{
  "errors": [ 
    "email is not valid", 
    "age is not valid", 
    "gender is not valid", 
    "video does not correspond to last given", 
    "video does not correspond to last given", 
    "userId does not exist" 
  ] 
}
```

On every *Register* request there should be created a dedicated actor which tracks this user activity.
Actor should track last video, such that if *Action* request sends id which was not given in previous response,
it will reply with error (one above).

On *Action* request routing mechanism should validate if actor for userId exists, otherwise 
send error response (one above).

Generic validation should be done on *Akka HTTP* layer with *Cats* such that all errors are merged in 
single error response.

Video ids (`videoId`) should be preconfigured and limited to 10 random `Long` values, *Action* should select 
least watched from the list.

Action ids (`actionId`) are limited to the `list [1, 2, 3]` where: 
- `1 - like` 
- `2 - skip` 
- `3 - play`. 
    
That does not affect logic, just for background info.

Application should be build with *SBT*, and latest versions of *Scala* and libraries.
After `sbt run` application should be up and running on 

```http request
http://localhost:8085 [/register] and [/action]
```

