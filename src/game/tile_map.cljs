(ns game.tile-map
  (:require
   [game.canvas :as canvas]))

(def TILE-SIZE 16)

(defn wall []
  {:type :WALL})

(defn blank []
  {:type :EMPTY})

(def game-map
  {:size 5
   :values [(wall) (wall)  (wall)  (wall)  (wall)
            (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (wall)
            (wall) (wall)  (wall)  (wall)  (wall)]})

(defn render-tile [tile x y h w]
  (let [colors  {:WALL "black"
                 :EMPTY "white"}]
    (canvas/draw-rect x y h w ((:type tile) colors))))

(defn render-map []
  (doseq [[i x] (map-indexed vector (:values game-map))]
    (render-tile
     x
     (* (mod i (:size game-map)) TILE-SIZE)
     (* (Math/floor (/ i (:size game-map))) TILE-SIZE)
     TILE-SIZE
     TILE-SIZE)))