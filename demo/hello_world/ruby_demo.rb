require "xmlrpc/client"
require "pathname"

client = XMLRPC::Client.new("127.0.0.1", "/", 10000)
ret = client.call("get_keyword_names")
client.call("run_keyword", "addImagePath", [Pathname.new(File.dirname(__FILE__)).realpath.to_s+"/img"])
client.call("run_keyword", "click", ["windows_start_menu.png"])
client.call("run_keyword", "waitUntilScreenContain", ["search_input.png", "5"])
client.call("run_keyword", "input_text", ["search_input.png", "notepad"])
client.call("run_keyword", "click", ["notepad.png"])
client.call("run_keyword", "doubleClick", ["notepad_title.png"])
client.call("run_keyword", "click", ["close.png"])