(ns todomvc.db
  (:require [cljs.reader]
            [cljs.spec :as s]
            [re-frame.core :as re-frame]))

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)
(s/def ::todo (s/keys :req-un [::id ::title ::done]))
(s/def ::todos (s/and
                (s/map-of ::id ::todo)
                #(instance? PersistentTreeMap %)))
(s/def ::showing #{:all :active :done})
(s/def ::db (s/keys :req-un [::todos ::showing]))

(def default-value
  {:todos (sorted-map)
   :showing :all})

(def ls-key "todos-reframe")

(defn todos->local-store
  "Puts todos into localStorage"
  [todos]
  (.setItem js/localStorage ls-key (str todos)))            ;; sorted-map writen as an EDN map

(re-frame/reg-cofx
 :local-store-todos
 (fn [cofx _]
   "Read in todos from localstore, and process into a map we can merge into app-db."
   (assoc cofx :local-store-todos
          (into (sorted-map)
                (some->> (.getItem js/localStorage ls-key)
                         (cljs.reader/read-string))))))
