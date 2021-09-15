(ns game.tile-map
  (:require
   [game.canvas :as canvas]))

(def TILE-SIZE 16)

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

(defn render-tile [tile x y h w]
  (let [colors  {:WALL "black"
                 :BLANK "white"}]
    (canvas/draw-rect x y h w ((:type tile) colors))))

(defn render-map []
  (doseq [[i x] (map-indexed vector (:values game-map))]
    (render-tile
     x
     (* (mod i (:size game-map)) TILE-SIZE)
     (* (Math/floor (/ i (:size game-map))) TILE-SIZE)
     TILE-SIZE
     TILE-SIZE)))

(defn get-tile [x y]
  (get (:values game-map) (+ x (* (:size game-map) y))))

(defn is? [type x y]
  (= (:type (get-tile x y)) type))