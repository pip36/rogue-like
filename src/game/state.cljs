(ns game.state
  (:require [game.canvas :as canvas]
            [game.config :as config]))

;;;; STATE DATA
(defn wall []
  {:type :WALL})

(defn blank []
  {:type :BLANK})

(def game-map
  {:size 15
   :values [(wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (wall) (wall) (wall) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)]})

(def player (atom {:x 2
                   :y 3}))

(def monsters (atom  {}))

(defn create-statue [statue-data]
  (merge statue-data config/statue))

(defn create-jelly [jelly-data]
  (merge jelly-data config/jelly))

;;;; State Queries?

(defn player-at? [x y]
  (and (= x (:x @player)) (= y (:y @player))))

(defn get-tile [x y]
  (get (:values game-map) (+ x (* (:size game-map) y))))

(defn tile-is? [type x y]
  (= (:type (get-tile x y)) type))

(defn all-monsters []
  (map last @monsters))

(defn get-monster-at [x y]
  (some
   #(and (= (:x %) x) (= (:y %) y) %)
   (all-monsters)))

;;;; Monster Movement
(defmulti movement :movement)

(defmethod movement :STATIC
  [monster]
  [(:x monster) (:y monster)])

(defmethod movement :RANDOM
  [monster]
  (let [x (:x monster)
        y (:y monster)
        choices [[x (inc y)] [x (dec y)]
                 [(inc x) y] [(dec x) y]]
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (player-at? x2 y2) [x y]
      (tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))


;;;; State Mutations
(defn kill-monster [id]
  (swap! monsters dissoc id))

(defn hurt-monster [id amount]
  (swap! monsters update-in [id :health] - amount))

(defmulti handle :type)

(defmethod handle :default [action]
  (. js/console error "No action handler defined for: " (:type action)))

(defn move-monster [id x y]
  {:type :MOVE_MONSTER :payload {:id id :x x :y y}})

(defmethod handle :MOVE_MONSTER [action]
  (swap! monsters conj {(-> action :payload :id) (merge (get @monsters (-> action :payload :id)) {:x (-> action :payload :x)
                                                                                                  :y (-> action :payload :y)})}))
(defn attack-monster [id]
  {:type :ATTACK_MONSTER :payload {:id id}})

(defmethod handle :ATTACK_MONSTER [action]
  (let [monster_id (-> action :payload :id)
        monster_health (-> @monsters monster_id :health)
        damage 5
        new_health (- monster_health damage)]
    (cond
      (<= new_health 0) (kill-monster monster_id)
      :else (hurt-monster monster_id damage))
    (. js/console log "Attacking" monster_id monster_health new_health)))

(defn trigger-actions [actions-list]
  (doseq [action actions-list]
    (handle action)))

;;;; Logic
(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [[id monster] (seq @monsters)]
    (let [[x y] (movement monster)]
      (trigger-actions [(move-monster id x y)]))))

(defn new-position [x y]
  [(+ (:x @player) x) (+ (:y @player) y)])

(defn move-player [x y] {:type :MOVE_PLAYER :payload {:x x :y y}})

(defmethod handle :MOVE_PLAYER [action]
  (swap! player conj {:x (-> action :payload :x)
                      :y (-> action :payload :y)}))

(defn try-move [[x y]]
  (let [monster (get-monster-at x y)]
    (cond
      (some? monster) (trigger-actions [(attack-monster (:id monster))])
      (tile-is? :BLANK x y) (trigger-actions  [(move-player x y)])
      :else nil)))

;;; INPUT
(defn handle-user-update [key]
  (case key
    :UP (try-move (new-position 0 -1))
    :DOWN (try-move (new-position 0 1))
    :LEFT (try-move (new-position -1 0))
    :RIGHT (try-move (new-position 1 0))
    :default nil))

;;;; RENDERING
(defn render-tile [tile x y h w]
  (let [colors  {:WALL "black"
                 :BLANK "white"}]
    (canvas/draw-rect x y h w ((:type tile) colors))))

(defn render-map []
  (doseq [[i x] (map-indexed vector (:values game-map))]
    (render-tile
     x
     (* (mod i (:size game-map)) config/TILE-SIZE)
     (* (Math/floor (/ i (:size game-map))) config/TILE-SIZE)
     config/TILE-SIZE
     config/TILE-SIZE)))

(defn render-player []
  (canvas/draw-rect
   (* config/TILE-SIZE (:x @player))
   (* config/TILE-SIZE (:y @player))
   config/TILE-SIZE config/TILE-SIZE
   "green"))

(defn render-monster [monster]
  (canvas/draw-rect
   (* config/TILE-SIZE (:x monster))
   (* config/TILE-SIZE (:y monster))
   config/TILE-SIZE config/TILE-SIZE
   (:color monster)))


(defn render-monsters []
  (doseq [[_ monster] (seq @monsters)]
    (render-monster monster)))


