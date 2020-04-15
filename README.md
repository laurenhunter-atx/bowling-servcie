# Bowling Service

## BUILD the application 
./gradlew clean build   

## BUILD AND UP Docker Compose 
docker-compose up --build   
docker-compose down <- down docker compose     

# API  
## POST /game/
curl https://localhost:8080/game 
```json
    {
       "players": [ {"name": "lolo" } ]
    }
```

## GET /game/id
curl https://localhost:8080/game/id 
```json
    {
      "id": "uuid",
      "frame": 1,
      "currentPlayerId": "uuid",
      "isGameComplete": false,
      "players": [
          {
              "id": "uuid",
               "name": "lolo",
               "score": 0,
               "rolls": [
                    { "pins": 2, "frame":  1, "throwForFrame":  1, "strike": false, "spare":  false }
                ]                 
          }               
      ]
    }
```

## POST /game/id/player/id/roll
curl https://localhost:8080/game/id/player/id/roll 
```json
{ "pins":  10, "frame":  1, "throwForFrame":  1 }
```


