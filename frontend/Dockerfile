FROM nginx:latest

RUN apt-get update && apt-get install -y unzip

WORKDIR /usr/share/nginx/html

COPY /build/libs/*.zip ./frontend.zip
RUN rm index.html && unzip -u frontend.zip && rm frontend.zip

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]