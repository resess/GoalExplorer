# frozen_string_literal: true

require 'nokogiri'
require_relative 'log'

# module for STG construction
module STG
  # Single node in Graph ADT for screens in STG
  class ScreenNode
    # only care about name currently
    attr_reader :name

    def initialize(name)
      @name = name
    end

    def ==(other)
      @name == other.name
    end

    def to_s
      @name
    end
    # attr_reader :name, :fragments, :dialogs
    #
    # # @param [String] name
    # # @param [Array] fragments
    # # @param [Array] dialogs
    # def initialize(name, fragments, dialogs)
    #   @name = name
    #   @fragments = fragments
    #   @dialogs = dialogs
    # end
    #
    # def ==(other)
    #   (@name == other.name) && (@fragments == other.fragments) && (@dialogs == other.dialogs)
    # end
    #
    # def to_s
    #   "[Activity=#{@name}, Fragments=#{@fragments}, Dialogs=#{@dialogs}]"
    # end
  end

  # edge in graph that displays the destination screen
  # and action to perform to reach destination screen
  class ScreenNodeEdge
    attr_reader :from, :to, :action

    # @param [ScreenNode] from
    # @param [ScreenNode] to
    # @param [Action] action
    def initialize(from, to, action)
      @from = from
      @to = to
      @action = action
    end

    def ==(other)
      (@from == other.from) && (@to == other.to) && (@action == other.action)
    end

    def to_s
      "From: #{@from}\nTo: #{@to}\nAction: #{@action}"
    end
  end

  # action used in edge to transition between screens taken from STG file
  class StaticAction
    attr_reader :ui_type, :handler, :resource_id

    # @param [String] ui_type
    # @param [String] handler
    # @param [String] resource_id
    def initialize(ui_type, handler, resource_id)
      @ui_type = ui_type
      @handler = handler
      @resource_id = resource_id
    end

    def ==(other)
      (@ui_type == other.ui_type) && (@handler == other.handler) && (@resource_id == other.resource_id)
    end

    def to_s
      "[Type=#{@ui_type}, Method=#{@handler}, ResourceID=#{@resource_id}]"
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
    attr_reader :targets

    def initialize
      # currently only care about name
      #
      # to find a node structure we traverse down multiple hashes
      # # will be @nodes[nameString][fragmentsArray][dialogsArray] to return node element
      # @nodes = Hash.new do |out_hash, out_key|
      #   out_hash[out_key] = Hash.new do |in_hash, in_key|
      #     in_hash[in_key] = {}
      #   end
      # end
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
      @targets = []
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

      @nodes[node.name] = node
    end

    # @param [String] name
    # @param [Array] fragments
    # @param [Array] dialogs
    def contains_node(name)
      # @nodes.key?(name) && @nodes[name].key?(fragments) && @nodes[name][fragments].key?(dialogs)
      @nodes.key?(name)
    end

    # @param [String] name
    # @param [Array] fragments
    # @param [Array] dialogs
    def node(name)
      unless contains_node(name)
        raise NodeDoesNotExistError,
              "Node with name #{name},
              Does not exists"
      end
      @nodes[name]
    end

    def count_nodes
      @nodes.count
    end

    def nodes
      @nodes.values
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

    # @param [ScreenNode] from
    # @param [ScreenNode] to
    def contains_edge(from, to)
      @edges[from].key?(to)
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

    def add_target(node)
      # node(node.name, node.fragments, node.dialogs)
      @targets << node
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
          paths.push(cur_path.dup)
          return
        end

        visited[cur_node] = true
        edges(cur_node).flatten(1).each do |edge|
          cur_path.push(edge)
          find_paths.call(edge.to)
          cur_path.pop
        end
        visited[cur_node] = false
      end

      find_paths.call(source)
      paths
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

  def self.construct_stg(file_name)
    graph = Graph.new

    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//ScreenNode').each do |screen_node|
        name = screen_node.at('name').content
        # fragments = screen_node.at('fragments').search('String').map(&:content).sort
        # dialogs = screen_node.at('dialogs').search('String').map(&:content).sort

        node = STG::ScreenNode.new(name)
        graph.add_node(node)
        graph.add_target(node) if screen_node.at('target').content == 'true'
      end
    end

    # # set all activity nodes as targets for our testing
    # graph.nodes.each { |node| graph.add_target(node) }



    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//ServiceNode').each do |screen_node|
        name = screen_node.at('name').content
        # fragments = screen_node.at('fragments').search('String').map(&:content).sort
        # dialogs = screen_node.at('dialogs').search('String').map(&:content).sort

        node = STG::ScreenNode.new(name)
        graph.add_node(node)
        # graph.add_target(node) if screen_node.at('target') == 'true'
      end
    end

    # parse screen nodes
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//BroadcastReceiverNode').each do |screen_node|
        name = screen_node.at('name').content
        # fragments = screen_node.at('fragments').search('String').map(&:content).sort
        # dialogs = screen_node.at('dialogs').search('String').map(&:content).sort

        node = STG::ScreenNode.new(name)
        graph.add_node(node)
        # graph.add_target(node) if screen_node.at('target') == 'true'
      end
    end

    # parse the transition edges
    File.open(file_name, 'r') do |file|
      Nokogiri::XML(file).xpath('//TransitionEdge').each do |edge|
        action = edge.at('edgeTag')
        next if action.nil?

        action = STG::StaticAction.new(
          action.at('typeOfUiElement').content,
          action.at('handlerMethod').content,
          action.at('resId').content
        )

        src = edge.at('srcNode')
        src_node = graph.node(
          src.at('name').content
          # src.at('fragments').search('String').map(&:content).sort,
          # src.at('dialogs').search('String').map(&:content).sort
        )

        tgt = edge.at('tgtNode')
        tgt_node = graph.node(
          tgt.at('name').content
          # tgt.at('fragments').search('String').map(&:content).sort,
          # tgt.at('dialogs').search('String').map(&:content).sort
        )

        graph.add_edge(STG::ScreenNodeEdge.new(src_node, tgt_node, action))
      end
    end

    graph
  end
end
