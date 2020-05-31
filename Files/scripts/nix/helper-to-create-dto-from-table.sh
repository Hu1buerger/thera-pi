#!/bin/bash
DEBUG=1

##########
#
# This was orginally written on a Mac & since the Mac (still) uses Bash <4.0 some of the nicer things are not avail - most noteably toUpper etc all
#   also the mysql-client & socket may not be where you expect them on a Linux machine
#
# Howto create a nice empty klasse + klasseDto from a table:
#
# - Check the MySQL params below - Socket, user, password and DBName
# - run skript it will create 3 section - 2 for the dto-class 1 for actual class
# - paste them to the according files
# - it will be quite ugly, since DBField-Names were taken directly if you want a nicer one, do the following
# - Only paste the class-member section to the class.java file
# - edit the names to your likings w/o changing the order & don't insert any indention
# - copy your version of the member-defs to a text-file and re-run this script with the param -ff TEXTFILEYOUCREATED
# - copy & paste the resulting 2 sections to the Dto-class
#
# - check the Dto-class reads the correct data-type from DB (rs.getString vs rs.getInt etc)
# - re-check the dataTypes for consistancy

_fromFile=0
_file=""

while [ ${#@} -gt 0 ]
do
	case "$1" in
		-ff)
			_fromFile=1
			_file="$2"
			shift 2
			;;
		*)
			echo "Unknown Parameter \"$1\" passed in"
			exit 1
			;;
	esac
done
[ "A$passw" == "A" ] && read -sp "Password: " passw && echo
[ "A$dbName" == "A" ] && read -p "Database name: " dbName && echo
[ "A$table" == "A" ] && read -p "Tablename: " table && echo

# Defaults (Maria-DB on Mac):
socket="/usr/local/mariadb/server/data/mysqld.sock"
user="lolibert"

mysqlcmd="mysql -u $user -S $socket -p$passw $dbName -N"
# _file="$1"

function trim() {
	echo $@
}

function toUpper() {
	echo "${1}" |tr '[:lower:]' '[:upper:]'
}

function toLower() {
	echo "${1}" |tr '[:upper:]' '[:lower:]'
}

function setFieldsInClass() {
	o=0
	cat << -EOT
    private RezeptFertige ofResultset(ResultSet rs) {
        RezeptFertige ret = new RezeptFertige();
        
        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not retrieve metaData", e);
            return null;
        }
        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                logger.debug("Checking: " + field + " in " + o);
                switch (field) {

-EOT
	for field in $fields
	do
		if [ $_fromFile -eq 0 ]
		then
			allups="$(toUpper $field )"
			alldowns="$(toLower $field )"
		else
			allups="$( toUpper ${fieldsDB[$o]} )"
			alldowns="$field"
		fi
		firstUp="${allups:0:1}${alldowns:1}"
		[ $DEBUG -gt 1 ] && echo "DEBUG: allups=\"$allups\""
		[ $DEBUG -gt 1 ] && echo "DEBUG: firstUp=\"$firstUp\""
		echo -ne "case \"$allups\":\n    ret.set${firstUp}(rs.getString(field));\n    break;\n"
		let o++
	done
	cat << -EOT
                default:
                    logger.error("Unhandled field in $table found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in fertige Rezepte");
            logger.error("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        
        return ret;
-EOT
}

function saveToDB() {
	o=0
	cat << -EOT
    public void saveToDB(RezeptFertige fertiges) {
        String sql = "insert into " + dbName + " set "
-EOT
	for field in $fields
	do
		if [ $_fromFile -eq 0 ]
                then
                        allups="$(toUpper $field )"
                        alldowns="$(toLower $field )"
                else
                        allups="$( toUpper ${fieldsDB[$o]} )"
                        alldowns="$field"
                fi
		firstUp="${allups:0:1}${alldowns:1}"
		echo -ne "+ \"${allups}='\" + fertiges.get"${firstUp}"() + \"',\"\n"
	done
	cat << -EOT
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save fertiges Rezept " + fertiges.toString() + " to Database", e);
        }
    }
-EOT
}

function varsInConstructor() {
	o=0
	for field in $fields
	do
		type="${types[$o]}"
		let o++
		if [[ $type =~ "char" ]] || [[ $type =~ "text" ]]
		then
			echo -ne "String "
		elif [[ $type =~ "enum('T'," ]]
		then
			echo -ne "boolean "
		else
			echo -ne "int "
		fi
		[ $DEBUG -gt 1 ] && echo "DEBUG type=\"$type\""
		echo "$( toLower $field );"
	done
}

if [ $_fromFile -eq 0 ]
then
	types=($( $mysqlcmd -t -e "show fields from $table"|cut -d'|' -f3| grep -v '+----' ))
	fields="$( $mysqlcmd -t -e "show fields from $table" |cut -d'|' -f2 | grep -v '+----' )"
	[ $DEBUG -gt 0 ] && echo "DEBUG: fields: \"$fields\""
else
	types=( $( function first() { echo $1; }; cat $_file| while read line;do echo $( first $line );done ) )
	fields="$( function scnd() { echo ${2/;/} ; }; cat $_file| while read line;do echo $( scnd $line );done )"
	fieldsDB=($( $mysqlcmd -t -e "show fields from $table" |cut -d'|' -f2 | grep -v '+----' ))
	[ $DEBUG -gt 1 ] && echo "Types: ${types[@]} and 1st: ${types[1]}" 
	[ $DEBUG -gt 0 ] && echo "Fields: \"$fields\""
fi

echo "Paste the following as \"ofResultSet\" method in Dto-class:"
echo "----------SNIP----------"
echo ""
setFieldsInClass
echo ""
echo "----------SNIP----------"
echo ""
echo "And paste the following as saveToDB method in Dto-class: "
echo "----------SNIP----------"
echo ""
saveToDB
echo ""
echo "----------SNIP----------"
echo ""
if [ $_fromFile -eq 0 ]
then
	echo "And paste the following as members of class:"
	echo "----------SNIP----------"
	echo ""
	varsInConstructor
fi
