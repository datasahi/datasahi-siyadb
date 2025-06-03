## datasahi siyadb

[Home Page](https://datasahi.com)

`datasahi siyadb` is a tool to query data stored in files from S3 using SQL.

---
### Features
- [x] query data in csv files in S3, using sql
- [x] easy to use, a single binary to start
- [x] cleans up downloaded files with configured time
- [x] supports multiple s3 buckets, from different regions and clouds, including minio
- [x] security via access keys and iam roles

### Planned
- [x] support multiple files in a single query
- [x] support parquet and json files
- [x] ability to create data, only inserts, no updates. Data will be stored in files in S3. 

---
### Helpful Info
- [x] can query only one file at a time
- [x] the tablename of a file is bucket_folder_filename, with no special characters

---
### Usage
- download the latest zip file from the releases. The zip file contains the jar file, a starter script and a sample config file
- needs jre 21 to execute
- create a env file or set these variables in environment
```shell
export DATASAHI_PORT=8082
export DATASAHI_WORK_DIR=/custom/work/dir
export DATASAHI_CONFIG_PATHS=/custom/path/datastores.json
```
Sample env file - `datasahi.env`
```shell
DATASAHI_PORT=8082
DATASAHI_WORK_DIR=/custom/work/dir
DATASAHI_CONFIG_PATHS=/custom/path/datastores.json
```
- use this command to start the server
 ```shell
  chmod +x start-datasahi-siyadb.sh
  ./start-datasahi-siyadb.sh
```

Sample config json - `datastores.json`
```json
{
  "datastores": [
    {
      "id": "as3store",
      "type": "S3",
      "name": "An S3 Store",
      "accessKey": "KEY",
      "secretKey": "KEY",
      "region": "us-east-1",
      "endpointUrl": "http://localhost:9444",
      "bucket": "mybucket",
      "folder": "",
      "cachedMinutes": 1
    }
  ]
}
```
A table name for a file stored in mybucket at /customer-invoices/invoices-2019.csv will be mybucket_customer_invoices_invoices_2019_csv. The table can be queried using the following SQL:
```java
        String tableName = datastoreid + "/" + bucket + "/" + folder + "/" + filename;
        return tableName.replaceAll("[^a-zA-Z0-9]", "_");
```

```sql
select * from mybucket_customer_invoices_invoices_2019_csv where customer_id = '12345' limit 10;
```
Duckdb sql is supported, please refer to [DuckDB documentation](https://duckdb.org/docs/sql/introduction) for more details on the SQL syntax. 

### Starting docker container

```shell
# Pull the latest image
docker pull datasahi/datasahi-siyadb:latest

# Run with default env values from datasahi-siyadb.env
docker run -p 8082:8082 datasahi-siyadb:0.1

# Override specific environment variables
docker run \
-p 8082:8082 \
-e DATASAHI_PORT=8082 \
-e DATASAHI_WORK_DIR=/custom/work/dir \
-e DATASAHI_CONFIG_PATHS=/custom/path/siyadb.json \
datasahi-siyadb:0.1

# Volume mount for custom configs
docker run \
-p 8082:8082 \
-v /local/path/to/config:/custom/path \
-v /local/path/to/work:/custom/work/dir \
datasahi-siyadb:0.1
```

### Stopping docker container

```shell
# Method 1: Docker stop
docker stop <container_id>

# Method 2: Use the included stop script (requires Docker exec)
docker exec <container_id> /app/stop.sh
```

### Sample queries via POST requests
**Request:**
```shell

curl -X POST 'http://localhost:8089/siyadb/query/execute' \
-H 'Content-Type: application/json' \
-d '{
  "datasource": "s3csp",
  "bucket": "cspdata",
  "filepath": "20240903.CSV",
  "filetype": "csv",
  "query": "SELECT * FROM s3csp_cspdata_20240903_CSV LIMIT 5"
}'
```

**Response:**
```json
{
  "data": {
    "records": [
      {
        "customerId": "C0001",
        "createdAt": "2024-09-04 03:59:50.0",
        "name": "ABC Consultants",
        "type": "ENTERPRISE"
      },
      {
        "customerId": "C0002",
        "createdAt": "2024-09-05 03:29:50.0",
        "name": "Foxtrot Ltd",
        "type": "SME"
      }
    ],
    "count": 2,
    "id": "67aa36dc-12d9-4b38-b478-60ec276362ce"
  },
  "success": true,
  "millis": 1062
}
```