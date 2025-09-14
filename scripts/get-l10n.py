import os
from shutil import copytree
import sys

def count_lines(filename):
  """Counts the number of lines in translation."""
  if not os.path.exists(filename):
    return 0
  with open(filename, 'r') as f:
    return sum(1 for line in f)

def copy_directory(src, dst, line_threshold):
  """Copies a directory if the strings.xml file has enough lines."""
  xml_file = os.path.join(src, "strings.xml")
  line_count = count_lines(xml_file)
  if line_count >= line_threshold:
    copytree(src, dst, dirs_exist_ok=True)
    print(f"Copied directory {src} to {dst} (lines: {line_count})")

# Get source by an argument
if len(sys.argv) < 2:
  print("Usage: python get-l10n.py <source_directory>")
  sys.exit(1)
source_dir = sys.argv[1]

line_threshold = 150  # Change this to your desired minimum

# cd to repo root
os.chdir(os.path.dirname(sys.argv[0]))
if (os.path.basename(os.getcwd()) == "scripts"):
  os.chdir("..")
 

# Loop through subdirectories
for root, _, files in os.walk(source_dir):
  for filename in files:
    if filename == "strings.xml":
      # Construct destination directory with same name as source
      dst_dir = os.path.join("app/src/main/res/", os.path.basename(root))
      copy_directory(root, dst_dir, line_threshold)
      break  # Only need to check one strings.xml per directory

