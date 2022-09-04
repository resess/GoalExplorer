  # frozen_string_literal: true


require 'nokogiri'
require_relative 'log'
require_relative 'util'


# module for STG construction
module STG
  # Single node in Graph ADT for screens in STG
  class ScreenNode
    attr_reader :name, :menu, :context_menu, :fragments, :drawer, :dialogs, :tab, :is_base
      
      #@param [String] name
      #@param [String] menu
      #@param [String] contextMenu
      #@param [String] drawer
      #@param [Array] fragments
      #@param [Array] dialogs
      def initialize(name, fragments, menu, context_menu, drawer, dialogs, tab, is_base)
        @name = name
        @fragments = fragments
        @menu = menu
        @context_menu = context_menu
        @drawer = drawer
        @dialogs = dialogs
        @tab = tab
        @is_base = is_base
      end

      def ==(other) #need to differentiate between precise matching for paths, and shallow matching for marking reached targets
        (@name == other.name) && (@menu == other.menu) && (@context_menu == other.context_menu) && (@drawer == other.drawer) && (@fragments == other.fragments) && (@dialogs == other.dialogs) && (@tab == other.tab)
      end

      def name
        @name
      end

      def menu
        @menu
      end

      def context_menu
        @context_menu
      end

      def drawer
        @drawer
      end

      def dialogs
        @dialogs
      end

      def tab
        @tab
      end

      def same_name(other)
        @name == other.name
      end

      def has_menu()
        !(@menu.nil?)
      end

      def has_context_menu()
        !(@context_menu.nil?)
      end

      def has_drawer()
        !(@drawer.nil?)
      end

      def has_dialogs()
        !(@dialogs.nil?)
      end

      def is_base
        @is_base
      end


      def to_s
        #@name
        "[Activity=#{@name}, Base=#{@is_base}, Menu=#{@menu}, ContextMenu=#{@context_menu}, Drawer=#{@drawer}, Dialogs=#{dialogs}, Tab=#{tab}]"
      end
  end

  #strategy to match two nodes in graph
  class NodeMatcher
    attr_reader :strategy

    def initialize(strategy)
      @strategy = strategy
    end

    def matches(node1, node2)
      #should it reason about targets rather than nodes here?
      case strategy
      when 'shallow'
        return node1.same_name(node2)
      when 'deep'
        return node1 == node2
      end
    end

    def closest_match(screen_nodes, fragments) #should I get the fragments here (cause technically no need to dump the fragments if we only want shallow matching)
      case strategy
      when 'shallow'
        screen_nodes.select {|screen_node| (screen_node.dialogs.nil? || screen_node.dialogs.empty?) }[0]
      when 'deep'
        screen_nodes.sort_by {|screen_node| (screen_node.fragments & fragments).length }[0]
      end
    end

  end

  # edge in graph that displays the destination screen
  # and action to perform to reach destination screen
  class ScreenNodeEdge
    attr_reader :from, :to, :action, :is_dead

    # @param [ScreenNode] from
    # @param [ScreenNode] to
    # @param [Action] action
    def initialize(from, to, action)
      @from = from
      @to = to
      @action = action
      @attempts = 0
      @is_dead = false
    end

    def ==(other)
      (@from == other.from) && (@to == other.to) && (@action == other.action)
    end

    def to_s
      "From: #{@from}\nTo: #{@to}\nAction: #{@action}\nAttempts: #{@attempts}"
    end

    def mark_status(status)
      @is_dead = status
    end

    def mark_dead()
      @is_dead = true
    end

    def is_dead
      @is_dead
    end

    def attempts
      @attempts
    end

    def reached_attempts_limit
      @attempts >= 50
    end

    def inc_attempts
      @attempts += 1
      if @attempts == 50
        @is_dead = true
      end
    end

    def reset
      @attempts = 0
      @is_dead = false
    end
  end

  # action used in edge to transition between screens taken from STG file
  class StaticAction
    attr_reader :ui_type, :handler, :resource_id, :parent_id, :content_desc, :text, :res_name

    # @param [String] ui_type
    # @param [String] handler
    # @param [String] resource_id
    # @param [String] parent_id
    # @param [String] content_desc
    def initialize(ui_type, handler, resource_id, parent_id, content_desc, text, res_name=nil)
      @ui_type = ui_type
      @handler = handler
      @resource_id = resource_id
      @parent_id = parent_id
      @content_desc = content_desc
      @text = text
      @res_name = res_name
    end

    def ==(other) #todo, figure out if dynamic action is the same as another static action
      (@ui_type == other.ui_type) && (@handler == other.handler) && (@resource_id == other.resource_id) && (@res_name == other.res_name)
    end

    def to_s
      "[Type=#{@ui_type}, Method=#{@handler}, ResourceID=#{@resource_id}, ResName=#{@res_name}, Text=#{@text}, ContentDesc=#{@content_desc}]"
    end
  end

  # action determined during traversal
  class DynamicAction
    attr_reader :action_list

    def initialize(action_list)
      @action_list = action_list
    end
  end

  # Graph ADT for the constructed STG
  class Graph
    attr_reader :targets, :matcher, :updatable

    def initialize(strategy, updatable)
      # currently only care about name
      #
      #the matcher
      @matcher = NodeMatcher.new(strategy)
      # to find a node structure we traverse down multiple hashes
      # # will be @nodes[nameString][fragmentsArray][dialogsArray] to return node element
      @nodes = Hash.new do |out_hash, out_key|
         out_hash[out_key] = Hash.new do |in_hash, in_key|
           in_hash[in_key] = {}
         end
      end
      @nodes = {}

      # to find the proper edge between two nodes traverse down multiple hashes
      # rely on using object references as key
      # will be @edges[nodeRef][nodeRef] to return list of edges between those nodes
      @edges = Hash.new do |out_hash, out_key|
        out_hash[out_key] = Hash.new do |in_hash, in_key|
          in_hash[in_key] = []
        end
      end

      # the nodes to try and reach
      @targets = {}
      # the targets to retry
      @unreached_targets = {}
      @updatable = updatable
      # the maximum number of tries per target
      @LIMIT = 5 
      # the dynamic max tries (incremented whenever there's an stg update)
      # i.e no need to retry if the stg was not updated
      @max_tries = 1
      # the highest number of tries ()
      @last_try = 0
      @updated_at_runtime = false
    end

    # @param [ScreenNode] node
    def add_node(node)
      # if contains_node(node.name, node.fragments, node.dialogs)
      #   raise NodeAlreadyExistsError,
      #         "Node with name #{node.name},
      #           Fragments: #{node.fragments},
      #           Dialogs: #{node.dialogs},
      #         Already exists"
      # end
      if !@nodes.key?(node.name) 
        @nodes[node.name] = [node]
      else 
        @nodes[node.name] << node
      end

    end

    # @param [String] name
    # @param [Array] fragments
    # @param [Array] dialogs
    def contains_node_with_name(name)
      # @nodes.key?(name) && @nodes[name].key?(fragments) && @nodes[name][fragments].key?(dialogs)
      @nodes.key?(name)
    end

    def contains_node(name, menu, drawer, dialogs, tab) #to rewrite
      @nodes.key?(name) && (@nodes[name].any? {|node| node.name == name && node.menu == menu && node.tab == tab && node.dialogs == dialogs})
    end

    def contains_node(name, menu, drawer, fragments, dialogs, tab) #to rewrite
      @nodes.key?(name) && (@nodes[name].any? {|node| node.name == name && node.menu == menu && node.tab == tab && node.dialogs == dialogs && node.fragments == node.fragments})
    end

    def contains_node(name, menu, context_menu, drawer, fragments, dialogs, tab) #to rewrite
      @nodes.key?(name) && (@nodes[name].any? {|node| node.name == name && node.menu == menu && node.context_menu == context_menu && node.tab == tab && node.dialogs == dialogs && node.fragments == fragments})
    end

    # @param [String] name
    # @param [Array] fragments
    # @param [Array] dialogs
    def node_by_name(name)
      unless contains_node_with_name(name)
        raise NodeDoesNotExistError,
              "Node with name #{name},
              Does not exists"
      end
      @nodes[name]
    end

    def base_node_by_name(name)
      unless contains_node_with_name(name)
        raise NodeDoesNotExistError,
              "Node with name #{name},
              Does not exists"
      end
      @nodes[name].select {|node| node.is_base == true}
    end

    def node(name, menu, drawer, fragments, dialogs, tab, is_base)
      unless contains_node(name, menu, drawer, fragments, dialogs, tab)
        raise NodeDoesNotExistError,
              "Node with name #{name}, menu #{menu}, drawer #{drawer}, dialogs #{dialogs}, tab #{tab}
              Does not exists"
      end
      @nodes[name].find{|node| node.menu == menu && node.drawer == drawer && node.tab == tab && node.dialogs == dialogs && node.is_base == is_base && node.fragments == fragments}
    end

    def node(name, menu, context_menu, drawer, fragments, dialogs, tab, is_base)
      unless contains_node(name, menu, context_menu, drawer, fragments, dialogs, tab)
        raise NodeDoesNotExistError,
              "Node with name #{name}, menu #{menu}, context menu #{context_menu}, drawer #{drawer}, dialogs #{dialogs}, tab #{tab}
              Does not exists"
      end
      @nodes[name].find{|node| node.menu == menu && node.context_menu == context_menu && node.drawer == drawer && node.tab == tab && node.dialogs == dialogs && node.is_base == is_base && node.fragments == fragments}
    end


    def count_nodes
      @nodes.count
    end

    def nodes
      @nodes.values.flat_map { |m| m }
    end

    def nodes_by_name(name)
      @nodes[name]
    end

    def get_closest_match(screen_nodes, fragments)
      matcher.closest_match(screen_nodes, fragments)
    end

    # @param [ScreenNodeEdge] edge
    def add_edge(edge)
      # no more edge checking as we can have multiple edges between nodes
      # if contains_edge(edge.from, edge.to)
      #   # prints a shortened error message, name is not a unique id for Nodes
      #   raise EdgeAlreadyExistsError, "Edge from #{edge.from.name} to #{edge.to.name} already exists"
      # end

      @edges[edge.from][edge.to] << edge
    end

    def add_edge_with_action(src_node, tgt_node, action)
      edge = STG::ScreenNodeEdge.new(src_node, tgt_node, action)
      if (!edge_exists(edge))
        Log.print "Updating stg with new edge #{edge}"
        #Log.print "Existing edges #{@edges[edge.from][edge.to]}"
        add_edge(edge)
        #whenever we update the stg, we wanna modify the last try index
        @max_tries = @last_try  + 1
        Log.print "Updating max try index to #{@max_tries}"
      end
      edge.inc_attempts()
      #@last_try = targets.min_by {|_, value| value[1]}[1] #we assume there is a target in store
      @updated_at_runtime = true
    end

    def add_edge_with_action_string(src_node, tgt_node, action_string)
      _, action_cmd, type, text = parseActionString(action_string)
      res_name = UTIL.get_action_resid(action_string)
      method = "on"+action_cmd.capitalize()#.split("(")[0]
      # TODO content desc?
      action = STG::StaticAction.new(type, method, nil, nil, nil, text, res_name)
      add_edge_with_action(src_node, tgt_node, action)
    end

    # @param [ScreenNode] from
    # @param [ScreenNode] to
    def contains_edge(from, to)
      @edges[from].key?(to)
    end

    def edge_exists(edge)
      @edges[edge.from].key?(edge.to) && @edges[edge.from][edge.to].include?(edge)
    end

    # @param [ScreenNode] from
    # @param [ScreenNode] to
    def edge(from, to)
      unless contains_edge(from, to)
        # prints a shortened error message, name is not a unique id for Nodes
        raise EdgeDoesNotExistsError, "Edge from #{from.name} to #{to.name} does not exist"
      end

      @edges[from][to]
    end

    # @param [ScreenNode] from
    def edges(from)
      # node(from.name, from.fragments, from.dialogs)
      @edges[from].values.flat_map { |m| m }
    end

    def targets
      @targets.keys
    end

    def reset_tries
      @targets.each {|key,val| val[1] = 0}
      @max_tries = 1
      @last_try = 0
    end

    def get_next_target
      #@targets.keys.reverse.select {|target| @targets[target][1] < @max_tries}.last
      # todo, when we try a diffeent launcher, we reset everything to 0?
      if @targets.nil? || @targets.empty?
        return nil
      end
      #might be tied to a different ruby version
      target = @targets.min_by {|_, value| value[1]}
      #TODO we don't need max_tries, just the index of the last_try
      if(!target.nil? && target[1][1] < @LIMIT && target[1][1] < @max_tries)
        target[0]
      else
        nil
      end
    end


    def all_targets
      #here I could return the merge with the unreachable put first?
      if @updatable
        @unreached_targets.keys + @targets.keys
      else
        @targets.keys
      end
    end

    def target_action(target)
      #@targets[target]
      @targets[target][0]
    end

    #here we can check if the stg is updatable, then we just move this target at the beginning instead of deleting it?
    # ALSO if we can't find a path and the stg has multiple launchers we can put it at the beginning?
    # HMM i DON't think the hashset is sorted anyways so ...
    def delete_target(target, found)
      #@targets.delete(target)
      if(!found)
        new_count = @targets[target][1] + 1
        @targets[target][1] = new_count
        @last_try = new_count if @last_try < new_count
        Log.print "Updating tries count to #{new_count} for #{target}"
      else
        @targets.delete(target)
      end
    end

    def delete_any_target(target, found)
      if(found)
        @unreached_targets.delete(target)
      else #target not found, add as unreached
        add_unreached_target(target)
      end
    end

    def add_unreached_target(target)
      @unreached_targets[target] = @targets[node] if @updatable
    end

    def add_target(node, action)
      # node(node.name, node.fragments, node.dialogs)
      #@targets[node] = action
      @targets[node] = [action, 0]
    end

    def is_target(screen_node)
      #todo check if function
      #all_targets = all_targets()
      all_targets = @targets.keys
      all_targets.include?(screen_node) || all_targets.any? {|node| @matcher.matches(screen_node, node)}
    end

    def mark_updated_at_runtime(status)
      @updated_at_runtime = status
    end

    def parse_action(action)
      type = action.at('typeOfUiElement').nil? ? nil : action.at('typeOfUiElement').content
      method = action.at('handlerMethod').nil? ? nil : action.at('handlerMethod').content
      id = action.at('resId').nil? ? nil : action.at('resId').content
      parent_id = action.at('parentId').nil? ? nil : action.at('parentId').content
      desc = action.at('contentDesc').nil? ? nil : action.at('contentDesc').content
      text = action.at('text').nil? ? nil : action.at('text').content
      STG::StaticAction.new(type, method, id, parent_id, desc, text)
    end


    def filter_by_action(path, action)
      return path.peek.action == action
    end

    def is_path_obsolete(path)
      #the last two edges are visited already (is that enough, what about false positives, like we set something/a setting and it changed everything)
      path.any? {|edge| edge.is_dead}
    end

    def is_path_redundant(path)
      #the last two edges are visited already (is that enough, what about false positives, like we set something/a setting and it changed everything)
      if (path.length > 1)
        path[path.length() -1].is_dead && path[path.length() - 2].is_dead
      else
        path[path.length() -1].is_dead
      end
    end

    # @param [ScreenNode] source
    # @param [ScreenNode] target
    # def shortest_path(source, target)
    #   # quick check that nodes are present within graph
    #   node(source.name, source.fragments, source.dialogs)
    #   node(target.name, target.fragments, target.dialogs)
    #
    #   # implementation of Dijkstra's
    #   # rely on object reference as unique ID for hashing
    #   distance = {}
    #   visited = {}
    #   parent = {}
    #
    #   nodes.each do |node|
    #     distance[node] = nil
    #     parent[node] = nil
    #     visited[node] = false
    #   end
    #
    #   distance[source] = 0
    #
    #   # returns reference of next node to visit, nil if there are no unvisited connected nodes
    #   next_to_visit = lambda {
    #     smallest_dist = next_node = nil
    #
    #     nodes.select { |node| visited[node] == false }.each do |node|
    #       smallest_dist = [smallest_dist, distance[node]].reject(&:nil?).min
    #       next_node = node if distance[node] == smallest_dist
    #     end
    #
    #     smallest_dist.nil? ? nil : next_node
    #   }
    #
    #   count_nodes.times do
    #     cur_node = next_to_visit.call
    #     visited[cur_node] = true
    #     break if cur_node.nil?
    #
    #     nodes.select { |node| visited[node] == false }
    #          .select { |node| contains_edge(cur_node, node) }
    #          .each do |node|
    #       distance[node] = [distance[cur_node] + 1, distance[node]].reject(&:nil?).min
    #       parent[node] = distance[node] == distance[cur_node] + 1 ? cur_node : parent[node]
    #     end
    #   end
    #
    #   return nil if parent[target].nil?
    #
    #   # retrace edges
    #   path = [edge(parent[target], target)]
    #   cur_node = target
    #   while parent[cur_node].nil?
    #     path << edge(parent[cur_node], cur_node)
    #     cur_node = parent[cur_node]
    #   end
    #   path.reverse
    # end

    def all_paths(source, target)
      # quick check that nodes are present within graph
      # node(source.name, source.fragments, source.dialogs)
      # node(target.name, target.fragments, target.dialogs)

      paths = []
      cur_path = []
      visited = Hash.new(false)

      # DFS to find best path
      find_paths = lambda do |cur_node|
        return if visited[cur_node]

        if cur_node == target
          #if not filter or filter(cur_path, criteria)
              #filter = cur_path.peek.action == edge.action
          paths.push(cur_path.dup)
          #end
          return
        end

        visited[cur_node] = true
        edges(cur_node).flatten(1).each do |edge|
          #Log.print ("Found #{edge}")
          cur_path.push(edge)
          find_paths.call(edge.to)
          cur_path.pop
        end
        visited[cur_node] = false
      end

      find_paths.call(source)
      paths
    end

    def reset_all_paths(paths)
      paths.each do |path|
        path.each do |edge|
          edge.reset()
        end
      end
    end


    class NodeAlreadyExistsError < StandardError
    end

    class NodeDoesNotExistError < StandardError
    end

    class EdgeAlreadyExistsError < StandardError
    end

    class EdgeDoesNotExistsError < StandardError
    end
  end



  def self.construct_stg(file_name, strategy, updatable)
    graph = Graph.new(strategy, updatable)

    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//ScreenNode').each do |screen_node|
        name = screen_node.at('name').content
        menu = screen_node.at('menu').nil? ? nil : screen_node.at('menu').content
        context_menu = screen_node.at('contextMenu').nil? ? nil : screen_node.at('contextMenu').content
        drawer = screen_node.at('drawer').nil? ? nil : screen_node.at('drawer').content
        fragments = screen_node.at('fragments').nil? ? nil : screen_node.at('fragments').search('string').map(&:content).sort
        dialogs = screen_node.at('dialogs').nil? ? nil : screen_node.at('dialogs').search('String').map(&:content).sort
        tab = screen_node.at('tab').nil? ? nil : screen_node.at('tab').content
        base = (screen_node.at('baseScreen').content == 'true') ? true : false

        node = STG::ScreenNode.new(name, fragments, menu, context_menu, drawer, dialogs, tab, base)
        graph.add_node(node)
        if strategy.eql?('deep') && screen_node.at('target').content == 'true'
          Log.print "Adding target #{node}"
          action = screen_node.at('targetAction').nil? ? nil : graph.parse_action(screen_node.at('targetAction'))
          graph.add_target(node, action)
        end
      end
    end

    # set all activity nodes as targets for our testing
    # only set base screen
    if strategy.eql?("shallow")
      graph.nodes.each { |node| graph.add_target(node,nil) if node.is_base() }
    end



    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//ServiceNode').each do |screen_node|
        name = screen_node.at('name').content
        # fragments = screen_node.at('fragments').search('String').map(&:content).sort
        # dialogs = screen_node.at('dialogs').search('String').map(&:content).sort

        node = STG::ScreenNode.new(name, nil, nil, nil, nil, nil, nil, true)
        graph.add_node(node)
        if strategy.eql?('deep') && !screen_node.at('target').nil? && screen_node.at('target').content == 'true'
          action = screen_node.at('targetAction').nil? ? nil : graph.parse_action(screen_node.at('targetAction'))
          graph.add_target(node, action)
        end
        #graph.add_target(node) if screen_node.at('target') == 'true'
      end
    end

    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//BroadcastReceiverNode').each do |screen_node|
        name = screen_node.at('name').content
        # fragments = screen_node.at('fragments').search('String').map(&:content).sort
        # dialogs = screen_node.at('dialogs').search('String').map(&:content).sort

        node = STG::ScreenNode.new(name, nil, nil, nil, nil, nil, nil, true)
        graph.add_node(node)
        if strategy.eql?('deep') && !screen_node.at('target').nil? && screen_node.at('target').content == 'true'
          action = screen_node.at('targetAction').nil? ? nil : graph.parse_action(screen_node.at('targetAction'))
          graph.add_target(node, action)
        end
        #graph.add_target(node) if screen_node.at('target') == 'true'
      end
    end

    # parse the transition edges
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//TransitionEdge').each do |edge|
        action = edge.at('edgeTag')
        action = graph.parse_action(action)
        next if action.nil?

        # action = STG::StaticAction.new(
        #   action.at('typeOfUiElement').content,
        #   action.at('handlerMethod').content,
        #   action.at('resId').content
        # )


        src = edge.at('srcNode')
        src_node = graph.node(
          src.at('name').content,
          src.at('menu').nil? ? nil : src.at('menu').content,
          src.at('contextMenu').nil? ? nil : src.at('contextMenu').content,
          src.at('drawer').nil? ? nil : src.at('drawer').content,
          src.at('fragments').nil? ? nil : src.at('fragments').search('string').map(&:content).sort,
          src.at('dialogs').nil? ? nil : src.at('dialogs').search('String').map(&:content).sort,
          src.at('tab').nil? ? nil : src.at('tab').content,
          (src.at('baseScreen').nil? || src.at('baseScreen').content == 'true') ? true : false

        )

        tgt = edge.at('tgtNode')
        tgt_node = graph.node(
          tgt.at('name').content,
          tgt.at('menu').nil? ? nil : tgt.at('menu').content,
          tgt.at('contextMenu').nil? ? nil : tgt.at('contextMenu').content,
          tgt.at('drawer').nil? ? nil : tgt.at('drawer').content,
          tgt.at('fragments').nil? ? nil : tgt.at('fragments').search('string').map(&:content).sort,
          tgt.at('dialogs').nil? ? nil : tgt.at('dialogs').search('String').map(&:content).sort,
          tgt.at('tab').nil? ? nil : tgt.at('tab').content,
          (tgt.at('baseScreen').nil? || tgt.at('baseScreen').content == 'true') ? true : false
        )
        #graph.add_target(edge) if edge.at('target') == 'true'
        #Log.print "Src #{src_node}, Target #{tgt_node}"
        if(!src_node.nil? && !tgt_node.nil? && !(src_node.name == tgt_node.name && !src_node.tab.nil? && src_node.tab != tgt_node.tab))
          graph.add_edge(STG::ScreenNodeEdge.new(src_node, tgt_node, action))
        end
      end
    end

    graph
  end

end
