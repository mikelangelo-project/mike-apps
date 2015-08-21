import os
import sys
import shutil
import tarfile
import argparse

#####################################################################
# Input:       1) Path to a new case
#              2) New image path name
#              3) Optional: OSv source path (or $OSV_HOME must be set)
#
# Description: This script uploads OpenFOAM case into OSv
#              after the OSv was built with an OpenFOAM image.
#
# Usage:       1) First, OSv has to be built with OpenFOAM image.
#                 You can do this with:
#
#                     $OSV_HOME./scripts/build image=OpenFOAM
#
#              2) You upload case with this script:
#
#./upload-case.py path/to/new/case/ /new/image/path/name path/to/OSv/
#
#              3) Run the image:
#
#                     $OSV_HOME./scripts/run.py -i new/image/path/name
#####################################################################

def extract(case_path, dst_path=None):
	import tarfile
	if dst_path is None:
		dst_path = os.path.dirname(case_path)
	tar = tarfile.open(case_path, 'r:gz')
	tar.extractall( dst_path )
	tar.close()


def get_abs_path(path):
	return os.path.abspath(path)


def copy_image(src, dst):
	shutil.copy2(src, dst)


def upload(osv_src_path, img_dst_path, manifest):
	# Upload a case into the image
	exit_upload = os.system(osv_src_path + "/scripts/upload_manifest.py -o " + img_dst_path + \
		" -m " + manifest)

	if exit_upload % 256 == 0:
		print "Upload successful."
	else:
		raise RuntimeError("Exit code " + str(exit_upload) + " from the command\nos.system(" + osv_src_path + "/scripts/upload_manifest.py -o " + img_dst_path + \
		" -m " + manifest + ")")

	# Correct cmdline
	exit_imgedit = os.system(osv_src_path + "/scripts/imgedit.py setargs " + img_dst_path + \
		" `cat cmdline`")

	if exit_imgedit % 256 == 0:
		print "Imgedit successful."
	else:
		print RuntimeError("Exit code " + str(exit_imgedit) + " from the command\nos.system(" + osv_src_path + "/scripts/upload_manifest.py -o " + img_dst_path + \
		" -m " + manifest + ")")


def main(case_path, img_dst_path, osv_src_path):

	# Get image source path
	if not osv_src_path:
		#osv_src_path = os.getenv('OSV_HOME', None)
		if osv_src_path is None:
			raise ValueError("Specify source image or set $OSV_HOME env variable.")

	if case_path is None or img_dst_path is None or osv_src_path is None:
		raise ValueError("ERROR: Please specify case path,	new image name and source path. $OSV_HOME can be set instead of source path.")

	# Get image source path
	img_src_path = os.path.join(osv_src_path, 'build/release.x64/usr.img')

	# Get absolute paths for input
	case_path    = get_abs_path(case_path)
	img_dst_path = get_abs_path(img_dst_path)
	osv_src_path = get_abs_path(osv_src_path)
	img_src_path = get_abs_path(img_src_path)

	# Extract tar.gz file if needed
	if case_path.endswith('.tar.gz'):
		extract(case_path)
		case_path = os.path.splitext(case_path)[0]  # Remove gz
		case_path = os.path.splitext(case_path)[0]  # Remove tar

	# Build manifest
	manifest = os.path.join( os.path.dirname(os.path.realpath(sys.argv[0])), "upload-case.manifest")
	with open(manifest, "w") as f:
		f.write("[manifest]\n")
		f.write("/openfoam/case/**: " + case_path + "/**")

	# Copy image
	if not img_src_path == img_dst_path:
		copy_image(img_src_path, img_dst_path)

	# Change directory (we need this for setting cmd line)
	new_wd = os.path.join(osv_src_path, "build/release.x64")
	os.chdir(new_wd)

	# Upload the case
	upload(osv_src_path, img_dst_path, manifest)

	# Remove manifest file  (warning: it must be absolute path)
	os.remove(manifest)


if __name__ == "__main__":

	parser = argparse.ArgumentParser(prog='upload-case')
	# Positional argument for case
	parser.add_argument("case", action="store", default=None, \
		help="path to case to be uploaded")
	# Positional argument for the destination image
	parser.add_argument("image", action="store", default=None, \
		help="path of the destination image")
	# Optional argument for source
	parser.add_argument("-s", "--source", action="store", default=os.getenv('OSV_HOME', None), \
		help="home folder of OSv, otherwise $OSV_HOME environmental variable is used")
	cmdargs = parser.parse_args()

	try:
		main(cmdargs.case, cmdargs.image, cmdargs.source)
	except Exception as e:
		print "ERROR:", e    # Print error
		parser.print_help()  # Print manual
