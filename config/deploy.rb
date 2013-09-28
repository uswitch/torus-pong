set :application, "torus-pong"
set :user, "deploy"
set :keep_releases, 3
set :deploy_to,           "/var/www/#{application}"
set :repository_cache,    "#{application}_cache"

set :repository,  "https://tgk@deveo.com/clojurecup/projects/pong/repositories/git/torus-pong"
set :scm, :git
set :ssh_options, {:forward_agent => true}
set :use_sudo, false
set :deploy_via, :remote_cache
set :scm_verbose, true
set :normalize_asset_timestamps, false

server '146.185.148.131', :app, :web, :db, :primary => true

namespace :deploy do

  desc "Build application as an uber jar"
  task :build_uber_jar do
    run("cd #{release_path}; lein uberjar")
    run("mv #{release_path}/target/torus-pong.jar #{release_path}/.")
  end

  after "deploy:symlink", "deploy:build_uber_jar"
end
