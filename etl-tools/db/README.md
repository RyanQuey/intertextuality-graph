## Add DB Migration
add it do play-app/db/migrations

## Run Migrations
```
../scripts/run_migrations.sh
```
setup.sh will run this for you once as well.

### Make a change to migration runner then run again?
```
../scripts/startup/_build-data-utils-jar.sh && ../run_migrations.sh
```

