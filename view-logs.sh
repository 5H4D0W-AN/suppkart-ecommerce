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

# Show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  -b, --backend     Show only backend logs"
    echo "  -f, --frontend    Show only frontend logs" 
    echo "  -a, --all         Show both logs (default)"
    echo "  -t, --tail [n]    Show last n lines (default: 50)"
    echo "  -w, --watch       Follow log files (live tail)"
    echo "  -h, --help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Show last 50 lines of both logs"
    echo "  $0 -w                 # Follow both logs live"
    echo "  $0 -b -t 100          # Show last 100 lines of backend log"
    echo "  $0 -f -w             # Follow frontend log live"
}

# Default values
SHOW_BACKEND=true
SHOW_FRONTEND=true
TAIL_LINES=50
WATCH_MODE=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -b|--backend)
            SHOW_BACKEND=true
            SHOW_FRONTEND=false
            shift
            ;;
        -f|--frontend)
            SHOW_BACKEND=false
            SHOW_FRONTEND=true
            shift
            ;;
        -a|--all)
            SHOW_BACKEND=true
            SHOW_FRONTEND=true
            shift
            ;;
        -t|--tail)
            TAIL_LINES="$2"
            shift 2
            ;;
        -w|--watch)
            WATCH_MODE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Check if log files exist
check_logs() {
    local backend_exists=false
    local frontend_exists=false
    
    if [ -f "backend.log" ]; then
        backend_exists=true
    fi
    
    if [ -f "frontend.log" ]; then
        frontend_exists=true
    fi
    
    if [ "$SHOW_BACKEND" = true ] && [ "$backend_exists" = false ]; then
        log_error "Backend log file not found"
        echo "Make sure the backend is running or has been started at least once."
        exit 1
    fi
    
    if [ "$SHOW_FRONTEND" = true ] && [ "$frontend_exists" = false ]; then
        log_error "Frontend log file not found"
        echo "Make sure the frontend is running or has been started at least once."
        exit 1
    fi
}

# Show static logs
show_static_logs() {
    if [ "$SHOW_BACKEND" = true ] && [ "$SHOW_FRONTEND" = true ]; then
        log "Showing last $TAIL_LINES lines from both logs"
        echo ""
        echo -e "${GREEN}=== BACKEND LOGS ===${NC}"
        tail -n "$TAIL_LINES" backend.log
        echo ""
        echo -e "${YELLOW}=== FRONTEND LOGS ===${NC}"
        tail -n "$TAIL_LINES" frontend.log
    elif [ "$SHOW_BACKEND" = true ]; then
        log "Showing last $TAIL_LINES lines from backend log"
        echo ""
        tail -n "$TAIL_LINES" backend.log
    elif [ "$SHOW_FRONTEND" = true ]; then
        log "Showing last $TAIL_LINES lines from frontend log"
        echo ""
        tail -n "$TAIL_LINES" frontend.log
    fi
}

# Show live logs
show_live_logs() {
    log "Starting live log monitoring..."
    log "Press Ctrl+C to stop"
    echo ""
    
    if [ "$SHOW_BACKEND" = true ] && [ "$SHOW_FRONTEND" = true ]; then
        tail -f backend.log | sed 's/^/[BACKEND] /' &
        BACKEND_TAIL_PID=$!
        
        tail -f frontend.log | sed 's/^/[FRONTEND] /' &
        FRONTEND_TAIL_PID=$!
        
        # Trap Ctrl+C to clean up tail processes
        trap 'kill $BACKEND_TAIL_PID $FRONTEND_TAIL_PID 2>/dev/null; exit 0' INT
        
    elif [ "$SHOW_BACKEND" = true ]; then
        tail -f backend.log &
        BACKEND_TAIL_PID=$!
        
        trap 'kill $BACKEND_TAIL_PID 2>/dev/null; exit 0' INT
        
    elif [ "$SHOW_FRONTEND" = true ]; then
        tail -f frontend.log &
        FRONTEND_TAIL_PID=$!
        
        trap 'kill $FRONTEND_TAIL_PID 2>/dev/null; exit 0' INT
    fi
    
    # Wait for user to stop
    wait
}

# Main execution
main() {
    log "SuppKart Log Viewer"
    log "==================="
    
    check_logs
    
    if [ "$WATCH_MODE" = true ]; then
        show_live_logs
    else
        show_static_logs
    fi
}

# Run main function
main
