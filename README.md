# Running the example

Run the postgres DB locally:  
`docker-compose up -d`

Call the `/passes/{passId}` endpoint with a tenantId query parameter, e.g.:
http://localhost:8080/passes/1?tenantId=tenant1
