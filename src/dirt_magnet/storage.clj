(ns dirt-magnet.storage
  (:require [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :refer [where]]
            [clojure.string :refer [split]]
            [io.pedestal.service.log :as log])
  (:import (java.net URI)))

(defn parse-db-url
  "Heroku-style DATABASE_URL is not exactly JDBC style. This builds an
  overriding db-spec map from it. If the URL contains no user, $USER
  from the environment is used with no password.

  Optionally takes an argument which is a string of Heroku DATABASE_URL
  format. If no argument is given, uses the one from the environment."
  [& [db-url]]
  (if-let [database-url (or db-url (System/getenv "DATABASE_URL"))]
    (let [dburi (URI. database-url)
          userinfo (.getUserInfo dburi)
          [username password] (if userinfo
                                (split userinfo #":")
                                [(System/getenv "USER") ""])
          host (.getHost dburi)
          port (.getPort dburi)
          path (.getPath dburi)]
      {:classname "org.postgresql.Driver"
       :subprotocol "postgresql"
       :user username
       :password password
       :subname (if (= -1 port)
                  (str "//" host path)
                  (str "//" host ":" port path))})
    (let [msg "No DATABASE_URL is present, cannot create connection."]
      (log/error :msg msg)
      (throw (Exception. msg)))))

(def ^:dynamic *db-conn* nil)
(def ^:dynamic *db-cache* (atom nil))

(defn mk-conn []
  (let [db-map (parse-db-url)]
    (assoc db-map :connection (j/get-connection db-map))))

(defn cache-conn
  "Creates a fresh connection and caches it."
  []
  (reset! *db-cache* (mk-conn)))

(defmacro with-database
  "Executes the body forms in a context where *db-conn* is available
  to be passed to any c.j.jdbc calls you may need to make. Implements
  the world's dumbest connection caching, consider using c3p0 someday
  or something like that."
  [& body]
  ;; TODO: detect when the cache is not nil but dead (try/catch?) and
  ;; retry with a new connection
  `(binding [*db-conn* (or @*db-cache* (cache-conn))]
     ~@body))

(def db-schema [:links
                [:id          :serial]
                [:title       :text]
                [:source      :text]
                [:url         :text]
                [:is_image    :boolean]
                [:created_at  "timestamp with time zone"]])

(defn apply-schema []
  (try
    (with-database
      (j/db-do-commands *db-conn*
                        false
                        (apply j/create-table-ddl db-schema))
      (j/db-do-commands *db-conn*
                        false
                        "CREATE UNIQUE INDEX link_id ON links (id);"))
    (catch Exception e
      (println e))))

(defn back-delete [table offset]
  (with-database
    (j/db-do-commands *db-conn*
                      false
                      (format (str "DELETE FROM %s"
                                   " WHERE ctid = ANY "
                                   "  (ARRAY"
                                   "   (SELECT ctid"
                                   "    FROM %s"
                                   "    ORDER BY created_at DESC"
                                   "     OFFSET %s))")
                              (name table)
                              (name table)
                              offset))))

(defn insert-into-table [table data]
  (with-database
    (j/insert! *db-conn* table data)))

(defn update-table [table data where-data]
  (with-database
    (j/update! *db-conn* table data (where where-data))))

(defn query [q]
  (with-database
    (j/query *db-conn* [q])))
