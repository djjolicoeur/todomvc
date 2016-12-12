(ns todomvc.events
  (:require [re-frame.core :as re-frame
             :refer [reg-event-db reg-event-fx inject-cofx
                     path trim-v after debug]]
            [todomvc.db :as db
             :refer [default-value todos->local-store]]
            [cljs.spec :as s]))


(defn check-and-throw
  [a-spec db]
  (when-not (s/valid? a-spec db )
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :todomvc.db/db)))

(def ->local-store
  (after todos->local-store))

(def todo-interceptors
  [check-spec-interceptor
   (path :todos)
   ->local-store
   (when ^boolean js/goog.DEBUG debug)
   trim-v])

(defn allocate-next-id
  [todos]
  ((fnil inc 0) (last (keys todos))))

(reg-event-fx
 :initialise-db
 [(inject-cofx :local-store-todos)
  check-spec-interceptor]
 (fn [{:keys [db local-store-todos]} _]
   {:db (assoc default-value :todos local-store-todos)}))



(reg-event-db
 :set-showing
 [check-spec-interceptor (path :showing) trim-v]
 (fn [old-keyword [new-filter-kw]]
   new-filter-kw))


(reg-event-db
 :add-todo
 todo-interceptors
 (fn [todos [text]]
   (let [id (allocate-next-id todos)]
     (assoc todos id {:id id :title text :done false}))))


(reg-event-db
 :toggle-done
 todo-interceptors
 (fn [todos [id]]
   (update-in todos [id :done] not)))

(reg-event-db
 :save
 todo-interceptors
 (fn [todos [id title]]
   (assoc-in todos [id :title] title)))


(reg-event-db
 :delete-todo
 todo-interceptors
 (fn [todos [id]]
   (dissoc todos id)))

(reg-event-db
 :clear-completed
 todo-interceptors
 (fn [todos _]
   (->> (vals todos)
        (filter :done)
        (map :id)
        (reduce dissoc todos))))


(reg-event-db
 :complete-all-toggle
 todo-interceptors
 (fn [todos _]
   (let [new-done (not-every? :done (vals todos))]
     (reduce #(assoc %1 [%2 :done] new-done)
             todos
             (keys todos)))))
