#!/bin/bash

# Function to upload a file
upload_file() {
  local serial_port="$1"
  local file_name="$2"

  ampy -p "$serial_port" put "$file_name" || echo "Error uploading $file_name"
}

# Check if ampy is available
if ! command -v ampy &> /dev/null; then
  echo "Error: ampy command not found. Please install ampy."
  exit 1
fi

# Check for serial port argument
if [ $# -lt 1 ]; then
  echo "Usage: $0 <serial_port>"
  exit 1
fi

# Get serial port from argument
serial_port="$1"

# Loop through all .py files
for file in *.py; do
  # Skip hidden files (starting with .)
  if [[ $file == .*.py ]]; then
    continue
  fi
  upload_file "$serial_port" "$file"
done

echo "Done uploading all .py files!"
