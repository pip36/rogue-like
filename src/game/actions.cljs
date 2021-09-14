(ns game.actions
  (:require
   [game.player :as p]
   [game.monster :as m]))

(defmulti handle :type)

(defn move-player [x y] {:type :MOVE_PLAYER :payload {:x x :y y}})

(defmethod handle :MOVE_PLAYER [action]
  (swap! p/player conj {:x (-> action :payload :x)
                        :y (-> action :payload :y)}))

(defn move-monster [x y] {:type :MOVE_MONSTER :payload {:x x :y y}})
(defmethod handle :MOVE_MONSTER [action]
  (swap! m/monster conj {:x (-> action :payload :x)
                         :y (-> action :payload :y)}))

(defmethod handle :default [])

(defn trigger-actions [actions-list]
  (doseq [action actions-list]
    (handle action)))