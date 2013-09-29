set :application, "torus-pong"
set :user, "deploy"
set :keep_releases, 3
set :deploy_to,           "/var/www/#{application}"
set :repository_cache,    "#{application}_cache"

set :repository,  "deveo@deveo.com:clojurecup/projects/pong/repositories/git/torus-pong"
set :scm, :git
set :ssh_options, {:forward_agent => true}
set :use_sudo, false
set :deploy_via, :remote_cache
set :scm_verbose, true
set :normalize_asset_timestamps, false

server '146.185.148.131', :app, :web, :db, :primary => true

namespace :deploy do

  desc "Restart Application"
  task :restart, :roles => :app, :except => { :no_release => true } do
    api.restart
  end

  desc "Build application as an uber jar"
  task :build_uber_jar do
    run("cd #{release_path}; lein uberjar")
    run("mv #{release_path}/target/torus-pong.jar #{release_path}/.")
  end

  after "deploy:symlink", "deploy:build_uber_jar"
end

namespace :api do

  task :install do
    put File.read(File.join(File.dirname(__FILE__), %w[upstart torus-pong.conf])), '/tmp/torus-pong.conf', :mode => '644'
    sudo "mv -f /tmp/torus-pong.conf /etc/init/torus-pong.conf"
  end

  task :start do
    sudo "start torus-pong"
  end

  task :stop do
    sudo "stop torus-pong"
  end

  task :restart do
    sudo "restart torus-pong"
  end
end

namespace :nginx do

  task :restart do
    sudo "/etc/init.d/nginx restart"
  end

  task :stop do
    sudo "/etc/init.d/nginx stop"
  end

  task :start do
    sudo "/etc/init.d/nginx start"
  end

  task :reload do
    sudo "/etc/init.d/nginx reload"
  end
end

namespace :tagging do
  task :push_deploy_tag do
    user = `git config --get user.name`.chomp
    email = `git config --get user.email`.chomp
    puts `git tag "clojurecup13" #{current_revision} -m "Deployed by #{user} <#{email}>"`
    puts `git push --tags origin`
  end
end
