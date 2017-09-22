(ns compojure.route-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [compojure.route :as route]))

(defn- map-subset? [m1 m2]
  (clojure.set/subset? (set m1) (set m2)))

(deftest not-found-route
  (testing "string body"
    (let [response ((route/not-found "foo") (mock/request :get "/"))]
      (is (= (:status response) 404))
      (is (= (:body response) "foo"))))
  (testing "response map body"
    (let [response ((route/not-found {:status 200 :body "bar"})
                    (mock/request :get "/"))]
      (is (= (:status response) 404))
      (is (= (:body response) "bar"))))
  (testing "async arity"
    (let [handler   (route/not-found "baz")
          response  (promise)
          exception (promise)]
      (handler (mock/request :get "/") response exception)
      (is (not (realized? exception)))
      (is (= (:status @response) 404))
      (is (= (:body @response) "baz")))))

(deftest resources-route
  (testing "text file"
    (let [route    (route/resources "/foo" {:root "test_files"})
          response (route (mock/request :get "/foo/test.txt"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "foobar\n"))
      (is (= (get-in response [:headers "Content-Type"])
            "text/plain"))))
  (testing "dotfile, implicitly allowed"
    (let [route    (route/resources "/foo" {:root "test_files"})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "bingbong\n"))))
  (testing "dotfile, explicitly allowed"
    (let [route    (route/resources "/foo" {:root "test_files"
                                            :dotfiles? (constantly true)})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "bingbong\n"))))
  (testing "dotfile, ignored"
    (let [route    (route/resources "/foo" {:root "test_files"
                                            :dotfiles? (constantly false)})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (nil? response))))
  (testing "dotfiles? function is called with the request"
    (let [actual-request (atom nil)
          mock-request   (mock/request :get "/foo/.dotfile")
          dotfiles?      (partial reset! actual-request)
          route          (route/resources "/foo" {:root "test_files"
                                                  :dotfiles? dotfiles?})]
      (route mock-request)
      (is (map-subset? (set mock-request) (set @actual-request))))))

(deftest files-route
  (testing "text file"
    (let [route    (route/files "/foo" {:root "test/test_files"})
          response (route (mock/request :get "/foo/test.txt"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "foobar\n"))
      (is (= (get-in response [:headers "Content-Type"])
             "text/plain"))))
  (testing "dotfile, implicitly allowed"
    (let [route    (route/files "/foo" {:root "test/test_files"})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "bingbong\n"))))
  (testing "dotfile, explicitly allowed"
    (let [route    (route/files "/foo" {:root "test/test_files"
                                        :dotfiles? (constantly true)})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "bingbong\n"))))
  (testing "dotfile, ignored"
    (let [route    (route/files "/foo" {:root "test/test_files"
                                        :dotfiles? (constantly false)})
          response (route (mock/request :get "/foo/.dotfile"))]
      (is (nil? response))))
  (testing "dotfiles? function is called with the request"
    (let [actual-request (atom nil)
          mock-request   (mock/request :get "/foo/.dotfile")
          dotfiles?      (partial reset! actual-request)
          route          (route/files "/foo" {:root "test_files"
                                              :dotfiles? dotfiles?})]
      (route mock-request)
      (is (map-subset? (set mock-request) (set @actual-request)))))
  (testing "root"
    (let [route    (route/files "/" {:root "test/test_files"})
          response (route (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) "<!doctype html><title></title>\n"))
      (is (= (get-in response [:headers "Content-Type"])
             "text/html")))))

(deftest head-method
  (testing "not found"
    (let [response ((route/not-found {:status 200
                                      :headers {"Content-Type" "text/plain"}
                                      :body "bar"})
                    (mock/request :head "/"))]
      (is (= (:status response) 404))
      (is (nil? (:body response)))
      (is (= (get-in response [:headers "Content-Type"])
             "text/plain"))))
  (testing "resources"
    (let [route    (route/resources "/foo" {:root "test_files"})
          response (route (mock/request :head "/foo/test.txt"))]
      (is (= (:status response) 200))
      (is (nil? (:body response)))
      (is (= (get-in response [:headers "Content-Type"])
             "text/plain"))))
  (testing "files"
    (let [route    (route/files "/foo" {:root "test/test_files"})
          response (route (mock/request :head "/foo/test.txt"))]
      (is (= (:status response) 200))
      (is (nil? (:body response)))
      (is (= (get-in response [:headers "Content-Type"])
             "text/plain")))))
