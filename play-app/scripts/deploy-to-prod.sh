# https://devcenter.heroku.com/articles/container-registry-and-runtime
# deploy by pushing image to docker. Requires more network io from local computer, but doesn't require Heroku to do all the network io
# might be faster just to let heroku do it though if local internet speeds are slow
# note that   `heroku container:push` will run docker build under the covers, but create a heroku-specific build (e.g., called registry.heroku.com/cambodia-charts/cambodia-in-charts)
# Still very slow, but we have to do all the network io here AND then push up the large image over the network!
# but don't have to do git commit and push

# only need to run this the first time on a given computer
# heroku login
# heroku container:login

# heroku container:push cambodia-in-charts  --app cambodia-charts
# heroku container:release cambodia-in-charts --app cambodia-charts

# Alt method: build in heroku
# https://devcenter.heroku.com/articles/build-docker-images-heroku-yml
# now heroku will look for the heroku.yml file
heroku stack:set container --app cambodia-charts
# then git push master
