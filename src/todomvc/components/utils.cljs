(ns todomvc.components.utils
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]))

(defn event-message [target f]
  (fn [& args]
    (reacl/return :message [target (apply f args)])))

(defn event-action [f]
  (fn [v]
    (reacl/return :action (f v))))

(defn- const-event [msg target]
  (when msg
    (fn [_]
      (reacl/send-message! target msg))))

(defn- key-event [f target]
  (when f
    (fn [e]
      (reacl/send-message! target (f (.. e -which))))))

(defn- pass [msg]
  msg)

(reacl/defclass button this [attrs label]
  render
  (dom/button (update attrs :onclick const-event this)
              label)
  
  handle-message pass)

(reacl/defclass label this [attrs & content]
  render
  (apply dom/label (update attrs :ondoubleclick const-event this)
         content)
  
  handle-message pass)

(reacl/defclass checkbox this checked? [& [attrs]]
  validate (assert (boolean? checked?))
  
  render
  (dom/input (merge attrs
                    {:type "checkbox"
                     :checked checked?
                     :onchange (fn [e]
                                 (reacl/send-message! this
                                                      (.. e -target -checked)))}))

  handle-message
  (fn [checked?]
    (reacl/return :app-state checked?)))

(defrecord Change [value])

(reacl/defclass text-input this value [attrs]
  validate (assert (string? value))
  
  render
  (dom/input (-> (merge {:type "text"} attrs)
                 (update :onblur const-event this)
                 (update :onkeydown key-event this)
                 (assoc :value value
                        :onchange (fn [e]
                                    (reacl/send-message! this (->Change (.. e -target -value)))))))
  
  handle-message
  (fn [msg]
    (if (instance? Change msg)
      (reacl/return :app-state (:value msg))
      (pass msg))))
