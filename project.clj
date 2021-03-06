(defproject dirt-magnet "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [io.pedestal/pedestal.service "0.1.9"]
                 [pedestal-content-negotiation/pedestal-content-negotiation "0.2.0"]

                 ;; Remove this line and uncomment the next line to
                 ;; use Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.1.6"]
                 ;; [io.pedestal/pedestal.tomcat "0.1.6"]

                 ;; auto-reload changes
                 [ns-tracker "0.2.1"]

                 ;; Logging
                 [ch.qos.logback/logback-classic "1.0.7"]
                 [org.slf4j/jul-to-slf4j "1.7.2"]
                 [org.slf4j/jcl-over-slf4j "1.7.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]
                 [org.clojure/java.jdbc "0.3.0-alpha3"]
                 [org.postgresql/postgresql "9.2-1003-jdbc4"]
                 [enlive "1.1.1"]
                 [clj-http "0.7.2"]
                 [bond "0.2.5"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :test-selectors {:default (constantly true)
                   :nodb (complement :database)}
  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources"]
  :main ^{:skip-aot true} dev)
