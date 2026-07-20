# Build context = frontend/ (Nx workspace root, cần cho npm workspaces resolve @org/shared-types)
FROM node:20-alpine
WORKDIR /workspace
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
EXPOSE 4300
CMD ["npx", "nx", "serve", "dispatcher-web", "--host", "0.0.0.0", "--port", "4300"]
