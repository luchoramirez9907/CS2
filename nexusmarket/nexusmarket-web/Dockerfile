# Builds the web and serves it with nginx
FROM node:26-alpine AS build

WORKDIR /web

COPY package.json package-lock.json ./
RUN npm ci --no-fund --no-audit

COPY index.html vite.config.js ./
COPY src ./src

# The address is fixed when the web is built: the browser is who calls the API
ARG VITE_API_URL=http://localhost:8080/api/v1
RUN VITE_API_URL="$VITE_API_URL" npm run build

FROM nginx:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
