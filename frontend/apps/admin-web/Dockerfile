# Build context = frontend/ (Nx workspace root, cần cho npm workspaces resolve @org/shared-types)
FROM node:20-alpine
WORKDIR /workspace
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
EXPOSE 4200
CMD ["npx", "nx", "serve", "admin-web", "--host", "0.0.0.0"]
