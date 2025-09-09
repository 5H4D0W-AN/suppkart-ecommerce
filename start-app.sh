#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Log function
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

# Check if processes are already running
check_existing_processes() {
    if lsof -ti:8080 >/dev/null 2>&1; then
        log_warning "Port 8080 is already in use. Stopping existing process..."
        lsof -ti:8080 | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
    
    if lsof -ti:3000 >/dev/null 2>&1; then
        log_warning "Port 3000 is already in use. Stopping existing process..."
        lsof -ti:3000 | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    if ! command -v java >/dev/null 2>&1; then
        log_error "Java is required but not installed"
        exit 1
    fi
    
    if ! command -v node >/dev/null 2>&1; then
        log_error "Node.js is required but not installed"
        exit 1
    fi
    
    if ! command -v npm >/dev/null 2>&1; then
        log_error "npm is required but not installed"
        exit 1
    fi
    
    log_success "All prerequisites are satisfied"
}

# Start backend
start_backend() {
    log "Starting backend server..."
    
    # Clean up old PID and log files
    rm -f .backend.pid backend.log
    
    # Start backend
    cd backend
    if [ -f "./mvnw" ]; then
        log "Using Maven wrapper to start backend"
        ./mvnw spring-boot:run > ../backend.log 2>&1 &
    else
        log "Using system Maven to start backend"
        mvn spring-boot:run > ../backend.log 2>&1 &
    fi
    
    BACKEND_PID=$!
    echo $BACKEND_PID > ../.backend.pid
    cd ..
    
    log "Backend started with PID: $BACKEND_PID"
    log "Waiting for backend to be ready..."
    
    # Wait for backend to start (check for port 8080)
    local max_attempts=60
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if lsof -ti:8080 >/dev/null 2>&1; then
            log_success "Backend is ready on port 8080"
            return 0
        fi
        
        # Check if backend process is still running
        if ! kill -0 $BACKEND_PID 2>/dev/null; then
            log_error "Backend process died unexpectedly"
            log "Last few lines from backend.log:"
            tail -10 backend.log
            return 1
        fi
        
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    log_error "Backend failed to start within expected time"
    log "Last few lines from backend.log:"
    tail -10 backend.log
    return 1
}

# Start frontend
start_frontend() {
    log "Starting frontend server..."
    
    # Clean up old PID and log files
    rm -f .frontend.pid frontend.log
    
    cd frontend
    
    # Install dependencies if node_modules doesn't exist
    if [ ! -d "node_modules" ]; then
        log "Installing frontend dependencies..."
        npm install
        if [ $? -ne 0 ]; then
            log_error "Failed to install frontend dependencies"
            cd ..
            return 1
        fi
    fi
    
    # Start frontend
    log "Starting frontend development server..."
    npm start > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../.frontend.pid
    cd ..
    
    log "Frontend started with PID: $FRONTEND_PID"
    
    # Wait a bit for frontend to start
    sleep 5
    
    # Check if frontend is running
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        log_success "Frontend is starting up"
    else
        log_error "Frontend process died unexpectedly"
        log "Last few lines from frontend.log:"
        tail -10 frontend.log
        return 1
    fi
}

# Show live logs
show_live_logs() {
    log "Starting live log monitoring..."
    log "Press Ctrl+C to stop monitoring (processes will continue running)"
    echo ""
    
    # Start tailing both logs in background
    if [ -f "backend.log" ]; then
        tail -f backend.log | sed 's/^/[BACKEND] /' &
        BACKEND_TAIL_PID=$!
    fi
    
    if [ -f "frontend.log" ]; then
        tail -f frontend.log | sed 's/^/[FRONTEND] /' &
        FRONTEND_TAIL_PID=$!
    fi
    
    # Trap Ctrl+C to clean up tail processes
    trap 'kill $BACKEND_TAIL_PID $FRONTEND_TAIL_PID 2>/dev/null; exit 0' INT
    
    # Wait for user to stop
    wait
}

# Main execution
main() {
    log "SuppKart Application Startup Script"
    log "===================================="
    
    check_existing_processes
    check_prerequisites
    
    if start_backend; then
        if start_frontend; then
            log_success "Both backend and frontend started successfully"
            log "Backend: http://localhost:8080"
            log "Frontend: http://localhost:3000"
            echo ""
            show_live_logs
        else
            log_error "Failed to start frontend"
            exit 1
        fi
    else
        log_error "Failed to start backend"
        exit 1
    fi
}

# Run main function
main
