(ns todomvc.session
  (:require [alandipert.storage-atom :refer [local-storage]]
            [todomvc.helpers :as helpers]))

(reset! alandipert.storage-atom/storage-delay 0)

(def todos (local-storage (atom helpers/todos-empty) :todos-reacl))
;; will look like {id {:id _ :title _ :completed _ }}

(def todos-counter (local-storage (atom 0) :todos-counter-reacl))
;; will inc for each new todo

(def todos-display-type (atom :all))
;; the options are :all, :active, :completed
