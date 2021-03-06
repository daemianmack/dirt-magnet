(ns dirt-magnet.storage-test
  (:require [clojure.test :refer :all]
            [dirt-magnet.storage :refer :all]
            [clojure.java.jdbc :as j]))

(deftest parse-db-url-does-what-we-think
  (let [database-url "postgres://localhost:5432/magnet"
        db-map (parse-db-url database-url)]
    ;; Note: everything else in the map is hardcoded.
    ;; If that ever changes, this test should be updated.
    (is (= (:subname db-map) "//localhost:5432/magnet"))))

(deftest ^:database test-with-database
  (reset! *db-cache* nil)
  (with-database
    (is (j/query *db-conn* ["SELECT 1=1"]))))

(deftest ^:database test-reestablish-connection
  (cache-conn)
  (.close (:connection @*db-cache*))
  (with-database
    (is (j/query *db-conn* ["SELECT 1=1"]))))
