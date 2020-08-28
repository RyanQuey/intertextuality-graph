# https://www.playframework.com/documentation/2.8.x/Deploying#Using-the-dist-task

# bash sbt-dist/bin/sbt-launch-lib.bash
# TODO add env var for secret

# run this after unziping to the project root (in docker: /app)

cambodia-in-charts-1.0-SNAPSHOT/bin/cambodia-in-charts \
  -Dhttp.port=${PORT:-9000} \
  -Dplay.http.secret.key=${APP_SECRET:-ad31779d4ee49d5ad5162bf1429c32e2e9933f3bs}
