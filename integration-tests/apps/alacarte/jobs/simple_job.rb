
class SimpleJob 

  def run() 
    $stderr.puts "Job executing!"
    basedir = ENV['BASEDIR']
    basedir = basedir.gsub( %r(\\:), ':' )
    basedir.gsub!( %r(\\\\), '\\' )
    $stderr.puts "BASEDIR #{basedir}"
    touchfile = File.join( basedir,  'target', 'touchfile.txt' )
    File.open( touchfile, 'w' ) do |f|
      f.puts( "Updated #{Time.now}" )
    end
  end

end
