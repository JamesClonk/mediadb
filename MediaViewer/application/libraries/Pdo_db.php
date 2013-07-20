<?php

if (!defined('BASEPATH'))
    exit('No direct script access allowed');

/**
 * PDO DB Class
 *
 * @package     CodeIgniter
 * @subpackage  Libraries
 * @category    Pdo_db
 * @author      JamesClonk
 */
class Pdo_db {

    private $dsn = NULL;
    private $username = NULL;
    private $password = NULL;
    private $options = NULL;
    private $conn = NULL;
    private $statements = array(array());
    private $autocommit = TRUE;

    const PDO_TEMP_STMT = 'PDO_DB_TEMPORARY_STATEMENT';

    function __construct($params = array()) {
        if (!array_key_exists("dsn", $params)) {
            if (!defined('ENVIRONMENT') || !file_exists($file_path = APPPATH . 'config/' . ENVIRONMENT . '/database' . EXT)) {
                if (!file_exists($file_path = APPPATH . 'config/database' . EXT)) {
                    show_error('The configuration file database' . EXT . ' does not exist');
                }
            }

            include($file_path);

            if (!isset($db) || count($db) == 0) {
                show_error('No database connection settings were found in the database config file');
            }

            if (isset($params['active_group']) && $params['active_group'] != '') {
                $active_group = $params['active_group'];
            }

            if (!isset($active_group) || !isset($db[$active_group])) {
                show_error('Invalid database connection group defined');
            }

            $params = array_merge($db[$active_group], $params);
        }

        if (!isset($params['dsn']) || $params['dsn'] == '') {
            show_error('No dsn for database connection defined');
        }

        $this->dsn = $params['dsn'];
        $this->username = isset($params['username']) ? $params['username'] : NULL;
        $this->password = isset($params['password']) ? $params['password'] : NULL;
        $this->options = isset($params['options']) ? $params['options'] : NULL;
        $this->_connect();

        $this->statements[self::PDO_TEMP_STMT]['STATEMENT'] = '';
        $this->statements[self::PDO_TEMP_STMT]['SQL'] = '';

        log_message('debug', "PDO DB Class Initialized");

        return $this;
    }

    function __destruct() {
        $this->_disconnect();
    }

    function _connect() {
        try {
            $this->conn = new PDO(
                            $this->dsn,
                            $this->username,
                            $this->password,
                            $this->options
                    ) or show_error("Cannot connect to database [$this->dsn]");
        } catch (PDOException $pex) {
            show_error("PDOException: " . $pex->getMessage());
        }
    }

    function _disconnect() {
        $this->_close_all_statements();
        $this->conn = NULL;
    }

    function _close_all_statements() {
        foreach ($this->statements as &$statement) {
            $statement['SQL'] = NULL;
            $statement['STATEMENT'] = NULL;
            $statement = NULL;
        }
        $this->statements = NULL;
    }

    public function get_dsn() {
        return $this->dsn;
    }

    public function get_username() {
        return $this->username;
    }

    public function get_options() {
        return $this->options;
    }

    public function get_connection() {
        return $this->conn;
    }

    public function commit() {
        try {
            $this->conn->commit() or show_error("Could not commit transaction");
        } catch (PDOException $pex) {
            show_error("PDOException: " . $pex->getMessage());
        }

        if (!$this->autocommit) {
            $this->conn->beginTransaction();
        }
    }

    public function rollback() {
        try {
            $this->conn->rollback() or show_error("Could not rollback transaction");
        } catch (PDOException $pex) {
            show_error("PDOException: " . $pex->getMessage());
        }

        if (!$this->autocommit) {
            $this->conn->beginTransaction();
        }
    }

    public function beginTransaction() {
        try {
            $this->conn->beginTransaction() or show_error("Could not begin new transaction");
        } catch (PDOException $pex) {
            show_error("PDOException: " . $pex->getMessage());
        }
    }

    public function get_autocommit() {
        return $this->autocommit;
    }

    public function set_autocommit($autocommit = TRUE) {
        if ($this->autocommit == $autocommit) {
            return;
        }
        $this->autocommit = $autocommit;
        $this->autocommit ? $this->commit() : $this->beginTransaction();
    }

    public function prepare_statement($statement_name, $sql) {
        $this->statements[$statement_name]['STATEMENT'] = $this->get_connection()->prepare($sql) or show_error("Cannot prepare statement [$statement_name]: [$sql]");
        $this->statements[$statement_name]['SQL'] = $sql;
        return $this->get_statement($statement_name);
    }

    public function get_statement($statement_name) {
        return $this->statements[$statement_name]['STATEMENT'];
    }

    public function is_statement($statement_name) {
        return isset($this->statements[$statement_name]['STATEMENT']);
    }

    public function query($sql, $paramaters = array(), $fetch_mode = PDO::FETCH_ASSOC) {
        if ($this->get_sql(self::PDO_TEMP_STMT) != $sql) {
            $this->prepare_statement(self::PDO_TEMP_STMT, $sql);
        }
        return $this->execute_query(self::PDO_TEMP_STMT, $paramaters, $fetch_mode);
    }

    public function execute_query($statement_name, $paramaters = array(), $fetch_mode = PDO::FETCH_ASSOC) {
        if (!$this->is_statement($statement_name)) {
            show_error("Unknown statement [$statement_name]");
        }
        $this->get_statement($statement_name)->setFetchMode($fetch_mode) or show_error("Cannot set fetch_mode on statement [$statement_name]");
        $this->get_statement($statement_name)->execute($paramaters) or show_error("Cannot execute statement [$statement_name]");
        return $this->get_statement($statement_name)->fetchAll();
    }

    public function update($sql, $paramaters = array()) {
        if ($this->get_sql(self::PDO_TEMP_STMT) != $sql) {
            $this->prepare_statement(self::PDO_TEMP_STMT, $sql);
        }
        return $this->execute_update(self::PDO_TEMP_STMT, $paramaters);
    }

    public function execute_update($statement_name, $paramaters = array()) {
        if (!$this->is_statement($statement_name)) {
            show_error("Unknown statement [$statement_name]");
        }
        $this->get_statement($statement_name)->execute($paramaters) or show_error("Cannot execute statement [$statement_name]");
        return $this->get_statement($statement_name)->rowCount();
    }

    public function get_sql($statement_name) {
        return $this->statements[$statement_name]['SQL'];
    }

}

/* End of file Pdo_db.php */
/* Location: ./application/libraries/Pdo_db.php */