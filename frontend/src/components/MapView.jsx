import { useEffect, useRef } from "react";
import mapboxgl from "mapbox-gl";
import { getMapboxToken } from "../utils/mapbox.js";

export default function MapView({ route, height = 260 }) {
  const containerRef = useRef(null);
  const mapRef = useRef(null);

  useEffect(() => {
    if (!containerRef.current) return;
    const token = getMapboxToken();
    if (!token) return;
    mapboxgl.accessToken = token;

    const start =
      route && route.startLng != null && route.startLat != null
        ? [route.startLng, route.startLat]
        : null;
    const end =
      route && route.endLng != null && route.endLat != null
        ? [route.endLng, route.endLat]
        : start;

    const center = start || [0, 0];

    const map = new mapboxgl.Map({
      container: containerRef.current,
      style: "mapbox://styles/mapbox/outdoors-v12",
      center,
      zoom: start ? 11 : 2
    });
    mapRef.current = map;

    map.on("load", () => {
      if (start && end) {
        const data = {
          type: "Feature",
          geometry: {
            type: "LineString",
            coordinates: [start, end]
          }
        };
        map.addSource("route-line", { type: "geojson", data });
        map.addLayer({
          id: "route-line-layer",
          type: "line",
          source: "route-line",
          paint: { "line-color": "#ff5500", "line-width": 4 }
        });
        map.addMarker = new mapboxgl.Marker({ color: "#0095f6" })
          .setLngLat(start)
          .addTo(map);
        if (end && (end[0] !== start[0] || end[1] !== start[1])) {
          new mapboxgl.Marker({ color: "#222" }).setLngLat(end).addTo(map);
        }
        const bounds = new mapboxgl.LngLatBounds();
        bounds.extend(start);
        bounds.extend(end);
        map.fitBounds(bounds, { padding: 40 });
      }
    });

    return () => {
      if (mapRef.current) {
        mapRef.current.remove();
      }
    };
  }, [route]);

  return (
    <div
      ref={containerRef}
      style={{ width: "100%", height, borderRadius: 6, overflow: "hidden" }}
    />
  );
}

