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

# Stop backend process
stop_backend() {
    log "Stopping backend server..."
    
    if [ -f ".backend.pid" ]; then
        BACKEND_PID=$(cat .backend.pid)
        if kill -0 $BACKEND_PID 2>/dev/null; then
            log "Sending SIGTERM to backend process (PID: $BACKEND_PID)..."
            kill $BACKEND_PID
            
            # Wait up to 15 seconds for graceful shutdown
            local count=0
            while [ $count -lt 15 ] && kill -0 $BACKEND_PID 2>/dev/null; do
                sleep 1
                count=$((count + 1))
                echo -n "."
            done
            echo ""
            
            # Force kill if still running
            if kill -0 $BACKEND_PID 2>/dev/null; then
                log_warning "Backend didn't shut down gracefully, force killing..."
                kill -9 $BACKEND_PID 2>/dev/null
            fi
            
            log_success "Backend process stopped"
        else
            log_warning "Backend PID file exists but process is not running"
        fi
        rm -f .backend.pid
    else
        log "No backend PID file found"
    fi
    
    # Also kill any process on port 8080
    if lsof -ti:8080 >/dev/null 2>&1; then
        log "Killing remaining processes on port 8080..."
        lsof -ti:8080 | xargs kill -9 2>/dev/null || true
        sleep 1
        
        if lsof -ti:8080 >/dev/null 2>&1; then
            log_error "Failed to free port 8080"
        else
            log_success "Port 8080 is now free"
        fi
    fi
}

# Stop frontend process
stop_frontend() {
    log "Stopping frontend server..."
    
    if [ -f ".frontend.pid" ]; then
        FRONTEND_PID=$(cat .frontend.pid)
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            log "Sending SIGTERM to frontend process (PID: $FRONTEND_PID)..."
            kill $FRONTEND_PID
            
            # Wait up to 10 seconds for graceful shutdown
            local count=0
            while [ $count -lt 10 ] && kill -0 $FRONTEND_PID 2>/dev/null; do
                sleep 1
                count=$((count + 1))
                echo -n "."
            done
            echo ""
            
            # Force kill if still running
            if kill -0 $FRONTEND_PID 2>/dev/null; then
                log_warning "Frontend didn't shut down gracefully, force killing..."
                kill -9 $FRONTEND_PID 2>/dev/null
            fi
            
            log_success "Frontend process stopped"
        else
            log_warning "Frontend PID file exists but process is not running"
        fi
        rm -f .frontend.pid
    else
        log "No frontend PID file found"
    fi
    
    # Also kill any process on port 3000
    if lsof -ti:3000 >/dev/null 2>&1; then
        log "Killing remaining processes on port 3000..."
        lsof -ti:3000 | xargs kill -9 2>/dev/null || true
        sleep 1
        
        if lsof -ti:3000 >/dev/null 2>&1; then
            log_error "Failed to free port 3000"
        else
            log_success "Port 3000 is now free"
        fi
    fi
}

# Clean log files
clean_logs() {
    if [ "$1" = "--clean-logs" ] || [ "$1" = "-c" ]; then
        log "Cleaning log files..."
        rm -f backend.log frontend.log
        log_success "Log files cleaned"
    fi
}

# Show live logs during shutdown
show_shutdown_logs() {
    log "Monitoring shutdown process..."
    log "Press Ctrl+C to skip log monitoring"
    echo ""
    
    # Show recent log entries if files exist
    if [ -f "backend.log" ]; then
        echo "Recent backend logs:"
        tail -5 backend.log | sed 's/^/[BACKEND] /'
        echo ""
    fi
    
    if [ -f "frontend.log" ]; then
        echo "Recent frontend logs:"
        tail -5 frontend.log | sed 's/^/[FRONTEND] /'
        echo ""
    fi
}

# Main execution
main() {
    log "SuppKart Application Shutdown Script"
    log "====================================="
    
    show_shutdown_logs
    
    stop_frontend
    stop_backend
    clean_logs "$1"
    
    log_success "Application shutdown complete"
}

# Run main function
main "$@"
